/*
 * This file is part of aion-unique <aion-unique.com>.
 *
 *  aion-unique is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-unique is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.services;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.dao.ItemStoneListDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Storage;
import com.aionemu.gameserver.model.gameobjects.player.StorageType;
import com.aionemu.gameserver.model.items.ItemId;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.templates.item.GodstoneInfo;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ADD_ITEMS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE_WAREHOUSE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UPDATE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UPDATE_WAREHOUSE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_WAREHOUSE_UPDATE;
import com.aionemu.gameserver.taskmanager.tasks.ItemUpdater;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * @author ATracer This class is used for Item manipulations (creation, disposing, modification) Can be used as a
 *         factory for Item objects
 */

public class ItemService
{
	private static Logger	log	= Logger.getLogger(ItemService.class);

	/**
	 * @param itemId
	 * @param count
	 * @return
	 * 
	 *         Creates new Item instance. If count is greater than template maxStackCount, count value will be cut to
	 *         maximum allowed This method will return null if ItemTemplate for itemId was not found.
	 */
	public static Item newItem(int itemId, long count)
	{
		ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemId);
		if(itemTemplate == null)
		{
			log.error("Item was not populated correctly. Item template is missing for item id: " + itemId);
			return null;
		}

		int maxStackCount = itemTemplate.getMaxStackCount();
		if(count > maxStackCount && maxStackCount != 0)
		{
			count = maxStackCount;
		}

		// TODO if Item object will contain ownerId - item can be saved to DB before return
		return new Item(0, IDFactory.getInstance().nextId(), itemTemplate, count, false, 0);
	}

	/**
	 * Loads item stones from DB for each item in a list if item is ARMOR or WEAPON Godstones will be laoded for WEAPON
	 * items
	 * 
	 * @param itemList
	 */
	public static void loadItemStones(List<Item> itemList)
	{
		if(itemList == null)
			return;
		DAOManager.getDAO(ItemStoneListDAO.class).load(itemList);
	}

	/**
	 * Used to split item into 2 items
	 * 
	 * @param player
	 * @param itemObjId
	 * @param splitAmount
	 * @param slotNum
	 * @param sourceStorageType
	 * @param desetinationStorageType
	 */
	public static void splitItem(Player player, int itemObjId, long splitAmount, int slotNum, int sourceStorageType,
		int destinationStorageType)
	{
		Storage sourceStorage = player.getStorage(sourceStorageType);
		Storage destinationStorage = player.getStorage(destinationStorageType);

		Item itemToSplit = sourceStorage.getItemByObjId(itemObjId);
		if(itemToSplit == null)
		{
			itemToSplit = sourceStorage.getKinahItem();
			if(itemToSplit.getObjectId() != itemObjId || itemToSplit == null)
			{
				log.warn(String.format("CHECKPOINT: attempt to split null item %d %d %d", itemObjId, splitAmount,
					slotNum));
				return;
			}
		}

		// To move kinah from inventory to warehouse and vise versa client using split item packet
		if(itemToSplit.getItemTemplate().isKinah())
		{
			if(!decreaseKinah(player, sourceStorage, splitAmount))
				return;
			increaseKinah(player, destinationStorage, splitAmount);
			return;
		}

		long oldItemCount = itemToSplit.getItemCount() - splitAmount;

		if(itemToSplit.getItemCount() < splitAmount || oldItemCount == 0)
			return;

		Item newItem = newItem(itemToSplit.getItemId(), splitAmount);
		newItem.setEquipmentSlot(slotNum);

		if(addFullItem(player, destinationStorage, newItem, false))
		{
			decreaseItemCount(player, sourceStorage, itemToSplit, splitAmount);
		}
	}

	public static boolean decreaseKinah(Player player, long amount)
	{
		return decreaseKinah(player, player.getInventory(), amount);
	}

	public static boolean decreaseKinah(Player player, Storage storage, long amount)
	{
		boolean operationResult = storage.decreaseKinah(amount);
		if(operationResult)
		{
			sendUpdateItemPacket(player, storage.getStorageType(), storage.getKinahItem());
		}
		return operationResult;
	}

	public static void increaseItemCount(Player player, Storage storage, Item item, long amount)
	{
		item.increaseItemCount(amount);
		sendUpdateItemPacket(player, storage.getStorageType(), item);
	}

	public static void increaseKinah(Player player, Storage storage, long amount)
	{
		storage.increaseKinah(amount);
		sendUpdateItemPacket(player, storage.getStorageType(), storage.getKinahItem());
	}

	public static void increaseKinah(Player player, long amount)
	{
		increaseKinah(player, player.getInventory(), amount);
	}

	/**
	 * Decrease Item Count for player inventory and send update packet.
	 * 
	 * @param player
	 * @param item
	 * @param count
	 * @return
	 */

	public static long decreaseItemCount(Player player, Item item, long count)
	{
		return decreaseItemCount(player, player.getInventory(), item, count);
	}

	public static boolean decreaseItemCountByItemId(Player player, int itemId, long count)
	{
		return decreaseItemCountByItemId(player, player.getInventory(), itemId, count);
	}

	/**
	 * Used to reduce item count in bag or completely remove by ITEMID This method operates in iterative manner overl
	 * all items with specified ITEMID. Return value can be the following: - true - item removal was successfull - false
	 * - not enough amount of items to reduce or item is not present
	 * 
	 * @param itemId
	 * @param count
	 * @return true or false
	 */
	public static boolean decreaseItemCountByItemId(Player player, Storage storage, int itemId, long count)
	{
		if(count < 1)
			return false;

		List<Item> items = storage.getItemsByItemId(itemId);

		for(Item item : items)
		{
			count = decreaseItemCount(player, storage, item, count);

			if(count == 0)
				break;
		}
		return count >= 0;
	}

	/**
	 * @param player
	 * @param storage
	 * @param item
	 * @param count
	 * @param persist
	 * @return
	 */

	public static long decreaseItemCount(Player player, Storage storage, Item item, long count)
	{
		long itemCount = item.getItemCount();
		if(itemCount >= count)
		{
			item.decreaseItemCount(count);
			count = 0;
		}
		else
		{
			item.decreaseItemCount(itemCount);
			count -= itemCount;
		}
		if(item.getItemCount() == 0)
		{
			removeItem(player, storage, item, true);
		}
		sendUpdateItemPacket(player, storage.getStorageType(), item);

		return count;
	}

	public static boolean removeItemFromInventory(Player player, Item item, boolean persist)
	{
		return removeItem(player, player.getInventory(), item, persist);
	}

	public static boolean removeItemFromInventoryByItemId(Player player, int itemId)
	{
		Storage storage = player.getInventory();
		boolean sucess = false;
		List<Item> items = storage.getItemsByItemId(itemId);

		for(Item item : items)
		{
			sucess |= removeItem(player, player.getInventory(), item, true);
		}
		return sucess;
	}

	public static boolean removeItemFromInventory(Player player, Item item)
	{
		return removeItem(player, player.getInventory(), item, true);
	}

	public static boolean removeItem(Player player, Storage storage, Item item, boolean persist)
	{
		return removeItem(player, storage, item, persist, true);
	}

	/**
	 * Used to remove item in inventory or completely remove by OBJECTID Return value can be the following: - true -
	 * item removal was successfull - false - not enough amount of items to reduce or item is not present
	 * 
	 * @param player
	 * @param itemObjId
	 * @return
	 */

	public static boolean removeItem(Player player, Storage storage, Item item, boolean persist, boolean sendPacket)
	{
		if(item == null)
		{ // the item doesn't exist, return false if the count is bigger then 0.
			log.warn("An item from player '" + player.getName() + "' that should be removed doesn't exist.");
			return false;
		}
		storage.removeFromBag(item, persist);
		if(sendPacket)
			sendDeleteItemPacket(player, storage.getStorageType(), item.getObjectId());
		return true;
	}

	/**
	 * Used to reduce item count in inventory or completely remove by OBJECTID Return value can be the following: - true
	 * - item removal was successfull - false - not enough amount of items to reduce or item is not present
	 * 
	 * @param player
	 * @param itemObjId
	 * @param count
	 * @return
	 */

	public static boolean removeItemByObjectId(Player player, int itemObjId, boolean persist)
	{
		return removeItemByObjectId(player, player.getInventory(), itemObjId, persist);
	}

	/**
	 * Used to reduce item count in bag or completely remove by OBJECTID Return value can be the following: - true -
	 * item removal was successfull - false - not enough amount of items to reduce or item is not present
	 * 
	 * @param player
	 * @param storage
	 * @param itemObjId
	 * @param count
	 * @return
	 */
	public static boolean removeItemByObjectId(Player player, Storage storage, int itemObjId, boolean persist)
	{
		Item item = storage.getItemByObjId(itemObjId);
		if(item == null)
		{ // the item doesn't exist, return false if the count is bigger then 0.
			log.warn("An item from player '" + player.getName() + "' that should be removed doesn't exist.");
			return false;
		}
		return removeItem(player, storage, item, persist);
	}

	/**
	 * Used to merge 2 items in inventory
	 * 
	 * @param player
	 * @param sourceItemObjId
	 * @param itemAmount
	 * @param destinationObjId
	 */
	public static void mergeItems(Player player, int sourceItemObjId, long itemAmount, int destinationObjId,
		int sourceStorageType, int destinationStorageType)
	{
		if(itemAmount == 0)
			return;

		if(sourceItemObjId == destinationObjId)
			return;

		Storage sourceStorage = player.getStorage(sourceStorageType);
		Storage destinationStorage = player.getStorage(destinationStorageType);

		Item sourceItem = sourceStorage.getItemByObjId(sourceItemObjId);
		Item destinationItem = destinationStorage.getItemByObjId(destinationObjId);

		if(sourceItem == null || destinationItem == null)
			return; // Invalid object id provided

		if(sourceItem.getItemTemplate().getTemplateId() != destinationItem.getItemTemplate().getTemplateId())
			return; // Invalid item type

		if(sourceItem.getItemCount() < itemAmount)
			return; // Invalid item amount

		ItemService.decreaseItemCount(player, sourceStorage, sourceItem, itemAmount);
		ItemService.increaseItemCount(player, destinationStorage, destinationItem, itemAmount);
	}

	public static void switchStoragesItems(Player player, int sourceStorageType, int sourceItemObjId,
		int replaceStorageType, int replaceItemObjId)
	{
		Storage sourceStorage = player.getStorage(sourceStorageType);
		Storage replaceStorage = player.getStorage(replaceStorageType);

		Item sourceItem = sourceStorage.getItemByObjId(sourceItemObjId);
		if(sourceItem == null)
			return;

		Item replaceItem = replaceStorage.getItemByObjId(replaceItemObjId);
		if(replaceItem == null)
			return;

		int sourceSlot = sourceItem.getEquipmentSlot();
		int replaceSlot = replaceItem.getEquipmentSlot();

		sourceItem.setEquipmentSlot(replaceSlot);
		replaceItem.setEquipmentSlot(sourceSlot);
		ItemService.removeItem(player, sourceStorage, sourceItem, false);
		ItemService.removeItem(player, replaceStorage, replaceItem, false);

		ItemService.addFullItem(player, sourceStorage, replaceItem);
		ItemService.addFullItem(player, replaceStorage, sourceItem);
	}

	/**
	 * Adds item count to player inventory I moved this method to service cause right implementation of it is critical
	 * to server operation and could cause starvation of object ids.
	 * 
	 * This packet will send necessary packets to client (initialize used only from quest engine
	 * 
	 * @param player
	 * @param itemId
	 * @param count
	 *            - amount of item that were not added to player's inventory
	 */
	public static boolean addItem(Player player, int itemId, long count)
	{
		if(GSConfig.LOG_ITEM)
			log.info(String.format("[ITEM] ID/Count - %d/%d to player %s.", itemId, count, player.getName()));
		if(itemId == ItemId.KINAH.value())
		{
			ItemService.increaseKinah(player, count);
			return true;
		}
		Item item = ItemService.newItem(itemId, count);
		if (item == null)
			return false;
		return addFullItem(player, player.getInventory(), item, true);
	}

	/**
	 * @param player
	 * @param itemId
	 * @param count
	 * @param manastones
	 * @param godStone
	 * @param enchantLevel
	 */
	public static boolean addFullItem(Player player, Storage storage, Item item)
	{
		return addFullItem(player, storage, item, true);
	}

	/**
	 * @param player
	 * @param itemId
	 * @param count
	 * @param manastones
	 * @param godStone
	 * @param enchantLevel
	 */
	public static boolean addFullItem(Player player, Storage storage, Item item, boolean merge)
	{
		ItemTemplate itemTemplate = item.getItemTemplate();
		if(itemTemplate == null)
			return false;

		int maxStackCount = itemTemplate.getMaxStackCount();
		int itemId = item.getItemId();

		if(itemId == ItemId.KINAH.value())
		{
			ItemService.increaseKinah(player, item.getItemCount());
			return true;
		}

		if(merge)
		{
			/**
			 * Increase count of existing items
			 */
			List<Item> existingItems = storage.getAllItemsByItemId(itemId); // look for existing in equipment. need for
			// power shards.
			for(Item existingItem : existingItems)
			{
				if(item.getItemCount() == 0)
					break;

				long freeCount = maxStackCount - existingItem.getItemCount();
				if(item.getItemCount() <= freeCount)
				{
					ItemService.increaseItemCount(player, storage, existingItem, item.getItemCount());
					item.setItemCount(0);
				}
				else
				{
					ItemService.increaseItemCount(player, storage, existingItem, freeCount);
					item.decreaseItemCount(freeCount);
				}
			}
		}
		/**
		 * Create new stacks
		 */

		while(!storage.isFull() && item.getItemCount() > 0)
		{
			// item count still more than maxStack value
			if(item.getItemCount() > maxStackCount)
			{
				Item newItem = newItem(itemId, maxStackCount);
				item.decreaseItemCount(maxStackCount);
				storage.putToBag(newItem);
				addItemPacket(player, storage.getStorageType(), newItem);
			}
			else
			{
				storage.putToBag(item);
				addItemPacket(player, storage.getStorageType(), item);
				return true;
			}
		}

		if(storage.isFull() && item.getItemCount() > 0)
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DICE_INVEN_ERROR);
			return false;
		}

		if(item.getItemCount() == 0)
		{
			item.setPersistentState(PersistentState.DELETED);
			ItemUpdater.getInstance().add(item);
		}
		return true;
	}

	/**
	 * 
	 * @param player
	 * @param itemObjId
	 * @param sourceStorageType
	 * @param destinationStorageType
	 * @param slot
	 */
	public static void moveItem(Player player, int itemObjId, int sourceStorageType, int destinationStorageType,
		int slot)
	{
		Storage sourceStorage = player.getStorage(sourceStorageType);
		Item item = player.getStorage(sourceStorageType).getItemByObjId(itemObjId);

		if(item == null)
			return;

		item.setEquipmentSlot(slot);

		if(sourceStorageType == destinationStorageType)
		{
			sendUpdateItemPacket(player, sourceStorageType, item);
			sourceStorage.setPersistentState(PersistentState.UPDATE_REQUIRED);
			return;
		}
		Storage destinationStorage = player.getStorage(destinationStorageType);
		if(ItemService.removeItem(player, sourceStorage, item, false))
			ItemService.addFullItem(player, destinationStorage, item);
	}

	public static void addItemPacket(Player player, int storageType, Item item)
	{
		if(storageType == StorageType.CUBE.getId())
			PacketSendUtility.sendPacket(player, new SM_ADD_ITEMS(Collections.singletonList(item)));
		else
			PacketSendUtility.sendPacket(player, new SM_WAREHOUSE_UPDATE(item, storageType));
	}

	private static void sendDeleteItemPacket(Player player, int storageType, int itemObjId)
	{
		if(storageType == StorageType.CUBE.getId())
			PacketSendUtility.sendPacket(player, new SM_DELETE_ITEM(itemObjId));
		else
			PacketSendUtility.sendPacket(player, new SM_DELETE_WAREHOUSE_ITEM(storageType, itemObjId));
	}

	private static void sendUpdateItemPacket(Player player, int storageType, Item item)
	{
		if(storageType == StorageType.CUBE.getId())
			PacketSendUtility.sendPacket(player, new SM_UPDATE_ITEM(item));
		else
			PacketSendUtility.sendPacket(player, new SM_UPDATE_WAREHOUSE_ITEM(item, storageType));
	}

	/**
	 * 
	 * @param itemId
	 */
	public static ManaStone addManaStone(Item item, int itemId)
	{
		if(item == null)
			return null;

		Set<ManaStone> manaStones = item.getItemStones();

		// temp fix for manastone spam till templates are updated
		if(manaStones.size() > 6)
			return null;

		int nextSlot = 0;
		boolean slotFound = false;

		Iterator<ManaStone> iterator = manaStones.iterator();
		while(iterator.hasNext())
		{
			ManaStone manaStone = iterator.next();
			if(nextSlot != manaStone.getSlot())
			{
				slotFound = true;
				break;
			}
			nextSlot++;
		}

		if(!slotFound)
			nextSlot = manaStones.size();

		ManaStone stone = new ManaStone(item.getObjectId(), itemId, nextSlot, PersistentState.NEW);
		manaStones.add(stone);

		return stone;
	}

	/**
	 * @param player
	 * @param itemObjId
	 * @param slotNum
	 */
	public static void removeManastone(Player player, int itemObjId, int slotNum)
	{
		Storage inventory = player.getInventory();
		Item item = inventory.getItemByObjId(itemObjId);
		if(item == null)
		{
			log.warn("Item not found during manastone remove");
			return;
		}

		if(!item.hasManaStones())
		{
			log.warn("Item stone list is empty");
			return;
		}

		Set<ManaStone> itemStones = item.getItemStones();

		if(itemStones.size() <= slotNum)
			return;

		int counter = 0;
		Iterator<ManaStone> iterator = itemStones.iterator();
		while(iterator.hasNext())
		{
			ManaStone manaStone = iterator.next();
			if(counter == slotNum)
			{
				manaStone.setPersistentState(PersistentState.DELETED);
				iterator.remove();
				DAOManager.getDAO(ItemStoneListDAO.class).store(Collections.singleton(manaStone));
				break;
			}
			counter++;
		}
		PacketSendUtility.sendPacket(player, new SM_UPDATE_ITEM(item));
	}

	/**
	 * @param player
	 * @param item
	 */
	public static void removeAllManastone(Player player, Item item)
	{
		if(item == null)
		{
			log.warn("Item not found during manastone remove");
			return;
		}

		if(!item.hasManaStones())
		{
			return;
		}

		Set<ManaStone> itemStones = item.getItemStones();
		Iterator<ManaStone> iterator = itemStones.iterator();
		while(iterator.hasNext())
		{
			ManaStone manaStone = iterator.next();
			manaStone.setPersistentState(PersistentState.DELETED);
			iterator.remove();
			DAOManager.getDAO(ItemStoneListDAO.class).store(Collections.singleton(manaStone));
		}

		PacketSendUtility.sendPacket(player, new SM_UPDATE_ITEM(item));
	}

	/**
	 * @param player
	 * @param weaponId
	 * @param stoneId
	 */
	public static void socketGodstone(Player player, int weaponId, int stoneId)
	{
		long socketPrice = player.getPrices().getPriceForService(100000);

		Item weaponItem = player.getInventory().getItemByObjId(weaponId);
		if(weaponItem == null)
		{
			PacketSendUtility
				.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_PROC_CANNOT_GIVE_PROC_TO_EQUIPPED_ITEM);
			return;
		}

		Item godstone = player.getInventory().getItemByObjId(stoneId);

		int godStoneItemId = godstone.getItemTemplate().getTemplateId();
		ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(godStoneItemId);
		GodstoneInfo godstoneInfo = itemTemplate.getGodstoneInfo();

		if(godstoneInfo == null)
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_PROC_NO_PROC_GIVE_ITEM);
			log.warn("Godstone info missing for itemid " + godStoneItemId);
			return;
		}

		if(!ItemService.decreaseKinah(player, socketPrice))
			return;
		weaponItem.addGodStone(godStoneItemId);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE
			.STR_GIVE_ITEM_PROC_ENCHANTED_TARGET_ITEM(new DescriptionId(Integer.parseInt(weaponItem.getName()))));
		ItemService.decreaseItemCount(player, godstone, 1);

		PacketSendUtility.sendPacket(player, new SM_UPDATE_ITEM(weaponItem));
	}

	public static boolean addItems(Player player, List<QuestItems> questItems)
	{
		int needSlot = 0;
		for(QuestItems qi : questItems)
		{
			if(qi.getItemId() != ItemId.KINAH.value() && qi.getCount() != 0)
			{
				int stackCount = DataManager.ITEM_DATA.getItemTemplate(qi.getItemId()).getMaxStackCount();
				int count = qi.getCount() / stackCount;
				if(qi.getCount() % stackCount != 0)
					count++;
				needSlot += count;
			}
		}
		if(needSlot > player.getInventory().getNumberOfFreeSlots())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.MSG_FULL_INVENTORY);
			return false;
		}
		for(QuestItems qi : questItems)
			addItem(player, qi.getItemId(), qi.getCount());
		return true;
	}

	/**
	 * @param player
	 */
	public static void restoreKinah(Player player)
	{
		// if kinah was deleted by some reason it should be restored with 0 count
		if(player.getStorage(StorageType.CUBE.getId()).getKinahItem() == null)
		{
			Item kinahItem = newItem(182400001, 0);

			onLoadHandler(player, player.getStorage(StorageType.CUBE.getId()), kinahItem);
		}

		if(player.getStorage(StorageType.ACCOUNT_WAREHOUSE.getId()).getKinahItem() == null)
		{
			Item kinahItem = newItem(182400001, 0);
			kinahItem.setItemLocation(StorageType.ACCOUNT_WAREHOUSE.getId());
			onLoadHandler(player, player.getStorage(StorageType.ACCOUNT_WAREHOUSE.getId()), kinahItem);
		}
	}

	/**
	 * 
	 * This method should be called only for new items added to inventory (loading from DB) If item is equiped - will be
	 * put to equipment if item is unequiped - will be put to default bag for now Kinah is stored separately as it will
	 * be used frequently
	 * 
	 * @param item
	 */
	public static void onLoadHandler(Player player, Storage storage, Item item)
	{
		if(player != null && item.isEquipped())
		{
			player.getEquipment().onLoadHandler(item);
		}
		else if(item.getItemTemplate().isKinah())
		{
			storage.setKinahItem(item);
		}
		else
		{
			storage.putToNextAvailableSlot(item);
		}
	}
}
