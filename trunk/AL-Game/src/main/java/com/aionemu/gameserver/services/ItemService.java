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

import java.util.ArrayList;
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
import com.aionemu.gameserver.model.items.GodStone;
import com.aionemu.gameserver.model.items.ItemId;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.templates.item.GodstoneInfo;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE_WAREHOUSE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UPDATE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UPDATE_WAREHOUSE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_WAREHOUSE_UPDATE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * @author ATracer
 *	This class is used for Item manipulations (creation, disposing, modification)
 *	Can be used as a factory for Item objects
 */
public class ItemService
{ 
	private static Logger log = Logger.getLogger(ItemService.class);

	/**
	 * @param itemId
	 * @param count
	 * @return
	 * 
	 * Creates new Item instance.
	 * If count is greater than template maxStackCount, count value will be cut to maximum allowed
	 * This method will return null if ItemTemplate for itemId was not found.
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

		//TODO if Item object will contain ownerId - item can be saved to DB before return
		return new Item(0, IDFactory.getInstance().nextId(), itemTemplate, count, false, 0);
	}

	/**
	 *  Loads item stones from DB for each item in a list if item is ARMOR or WEAPON
	 *  Godstones will be laoded for WEAPON items
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
	 *  Used to split item into 2 items
	 *  
	 * @param player
	 * @param itemObjId
	 * @param splitAmount
	 * @param slotNum
	 * @param sourceStorageType
	 * @param desetinationStorageType
	 */
	public static void splitItem (Player player, int itemObjId, long splitAmount, int slotNum, int sourceStorageType, int destinationStorageType)
	{
		Storage sourceStorage = player.getStorage(sourceStorageType);
		Storage destinationStorage = player.getStorage(destinationStorageType);

		Item itemToSplit = sourceStorage.getItemByObjId(itemObjId);
		if(itemToSplit == null)
		{
			itemToSplit = sourceStorage.getKinahItem();
			if(itemToSplit.getObjectId() != itemObjId || itemToSplit == null)
			{
				log.warn(String.format("CHECKPOINT: attempt to split null item %d %d %d", itemObjId, splitAmount, slotNum));
				return;
			}
		}

		// To move kinah from inventory to warehouse and vise versa client using split item packet
		if(itemToSplit.getItemTemplate().isKinah())
		{
			moveKinah(player, sourceStorage, splitAmount);
			return;
		}

		long oldItemCount = itemToSplit.getItemCount() - splitAmount;
		
		if(itemToSplit.getItemCount()<splitAmount || oldItemCount == 0)
			return;

		Item newItem = newItem(itemToSplit.getItemTemplate().getTemplateId(), splitAmount);
		newItem.setEquipmentSlot(slotNum);
		if(destinationStorage.putToBag(newItem) != null)
		{
			sourceStorage.decreaseItemCount(itemToSplit, splitAmount);

			List<Item> itemsToUpdate = new ArrayList<Item>();
			itemsToUpdate.add(newItem);

			sendStorageUpdatePacket(player, destinationStorageType, itemsToUpdate.get(0));
			sendUpdateItemPacket(player, sourceStorageType, itemToSplit);
		}		
		else
		{
			releaseItemId(newItem);
		}
	}


	private static void moveKinah(Player player, Storage source, long splitAmount)
	{
		if(source.getKinahItem().getItemCount() < splitAmount)
			return;

		switch(source.getStorageType())
		{
			case 0:
			{
				Storage destination = player.getStorage(StorageType.ACCOUNT_WAREHOUSE.getId());
				long chksum = (source.getKinahItem().getItemCount() - splitAmount) + (destination.getKinahItem().getItemCount() + splitAmount);

				if(chksum != source.getKinahItem().getItemCount() + destination.getKinahItem().getItemCount())
					return;

				source.decreaseKinah(splitAmount);
				destination.increaseKinah(splitAmount);
				break;
			}

			case 2:
			{
				Storage destination = player.getStorage(StorageType.CUBE.getId());
				long chksum = (source.getKinahItem().getItemCount() - splitAmount) + (destination.getKinahItem().getItemCount() + splitAmount);

				if(chksum != source.getKinahItem().getItemCount() + destination.getKinahItem().getItemCount())
					return;

				source.decreaseKinah(splitAmount);
				destination.increaseKinah(splitAmount);
				break;
			}
			default:
				break;
		}
	}

	/**
	 *  Used to merge 2 items in inventory
	 *  
	 * @param player
	 * @param sourceItemObjId
	 * @param itemAmount
	 * @param destinationObjId
	 */
	public static void mergeItems (Player player, int sourceItemObjId, long itemAmount, int destinationObjId, int sourceStorageType, int destinationStorageType)
	{
		if(itemAmount == 0)
			return;

		if (sourceItemObjId == destinationObjId)
			return;

		Storage sourceStorage = player.getStorage(sourceStorageType);
		Storage destinationStorage = player.getStorage(destinationStorageType);

		Item sourceItem = sourceStorage.getItemByObjId(sourceItemObjId);
		Item destinationItem = destinationStorage.getItemByObjId(destinationObjId);

		if(sourceItem == null || destinationItem == null)
			return; //Invalid object id provided

		if(sourceItem.getItemTemplate().getTemplateId() != destinationItem.getItemTemplate().getTemplateId())
			return; //Invalid item type

		if(sourceItem.getItemCount() < itemAmount)
			return; //Invalid item amount

		if(sourceItem.getItemCount() == itemAmount)
		{
			destinationStorage.increaseItemCount(destinationItem, itemAmount);
			sourceStorage.removeFromBag(sourceItem, true);

			sendDeleteItemPacket(player, sourceStorageType, sourceItem.getObjectId());
			sendUpdateItemPacket(player, destinationStorageType, destinationItem);

		}
		else if(sourceItem.getItemCount() > itemAmount)
		{
			sourceStorage.decreaseItemCount(sourceItem, itemAmount);
			destinationStorage.increaseItemCount(destinationItem, itemAmount);

			sendUpdateItemPacket(player, sourceStorageType, sourceItem);
			sendUpdateItemPacket(player, destinationStorageType, destinationItem);
		}
		else return; // cant happen in theory, but...
	}

	public static void switchStoragesItems(Player player, int sourceStorageType, int sourceItemObjId, int replaceStorageType, int replaceItemObjId)
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

		sourceStorage.removeFromBag(sourceItem, false);
		replaceStorage.removeFromBag(replaceItem, false);

		Item newSourceItem = sourceStorage.putToBag(replaceItem);
		Item newReplaceItem = replaceStorage.putToBag(sourceItem);

		sendDeleteItemPacket(player, sourceStorageType, sourceItemObjId);
		sendStorageUpdatePacket(player, sourceStorageType, newSourceItem);

		sendDeleteItemPacket(player, replaceStorageType, replaceItemObjId);
		sendStorageUpdatePacket(player, replaceStorageType, newReplaceItem);
	}

	/**
	 *  Adds item count to player inventory
	 *  I moved this method to service cause right implementation of it is critical to server
	 *  operation and could cause starvation of object ids.
	 *  
	 *  This packet will send necessary packets to client (initialize used only from quest engine
	 *  
	 * @param player
	 * @param itemId
	 * @param count - amount of item that were not added to player's inventory
	 */
	public static long addItem(Player player, int itemId, long count)
	{
		if(GSConfig.LOG_ITEM)
			log.info(String.format("[ITEM] ID/Count - %d/%d to player %s.", itemId, count, player.getName()));
		
		return addFullItem(player, itemId, count, null, null, 0);
	}

	/**
	 * @param player
	 * @param itemId
	 * @param count
	 * @param isQuestItem
	 * @param manastones
	 * @param godStone
 	 * @param enchantLevel
	 */
	public static long addFullItem(Player player, int itemId, long count, Set<ManaStone> manastones, GodStone godStone, int enchantLevel)
	{
		Storage inventory = player.getInventory();

		ItemTemplate itemTemplate =  DataManager.ITEM_DATA.getItemTemplate(itemId);
		if(itemTemplate == null)
			return count;

		int maxStackCount = itemTemplate.getMaxStackCount();

		if (itemId == ItemId.KINAH.value())
		{
			inventory.increaseKinah(count);
			return 0;
		}
		else
		{
			/**
			 * Increase count of existing items
			 */
			List<Item> existingItems = inventory.getAllItemsByItemId(itemId); // look for existing in equipment. need for power shards.
			for(Item existingItem : existingItems)
			{
				if(count == 0)
					break;

				long freeCount = maxStackCount - existingItem.getItemCount();
				if(count <= freeCount)
				{
					inventory.increaseItemCount(existingItem, count);
					count = 0;
				}
				else
				{
					inventory.increaseItemCount(existingItem, freeCount);
					count -= freeCount;
				}

				updateItem(player, existingItem, false);
			}

			/**
			 * Create new stacks
			 */

			while(!inventory.isFull() && count > 0)
			{
				// item count still more than maxStack value
				if(count > maxStackCount)
				{
					Item item = newItem(itemId, maxStackCount);
					count -= maxStackCount;
					inventory.putToBag(item);
					updateItem(player, item, true);
				}
				else
				{
					Item item = newItem(itemId, count);
					
					//add item stones if available
					//1. manastones
					if(manastones != null)
					{
						for(ManaStone manaStone : manastones)
						{
							addManaStone(item, manaStone.getItemId());
						}
					}
					//2. godstone
					if(godStone != null)
					{
						item.addGodStone(godStone.getItemId());
					}
					//3. enchantLevel
					if(enchantLevel > 0)
					{
						item.setEnchantLevel(enchantLevel);
					}
					inventory.putToBag(item);
					updateItem(player, item, true);
					count = 0;
				}
			}

			if(inventory.isFull() && count > 0)
			{
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DICE_INVEN_ERROR);
			}

			return count;
		}
	}

	/**
	 * 
	 * @param player
	 * @param itemObjId
	 * @param sourceStorageType
	 * @param destinationStorageType
	 * @param slot
	 */
	public static void moveItem(Player player, int itemObjId, int sourceStorageType, int destinationStorageType, int slot)
	{
		Storage sourceStorage = player.getStorage(sourceStorageType);
		Item item = player.getStorage(sourceStorageType).getItemByObjId(itemObjId);

		if(item == null)
			return;

		item.setEquipmentSlot(slot);

		if (sourceStorageType == destinationStorageType)
		{
			sourceStorage.setPersistentState(PersistentState.UPDATE_REQUIRED);
			return;
		}		

		Storage destinationStorage = player.getStorage(destinationStorageType);
		List<Item> existingItems = destinationStorage.getItemsByItemId(item.getItemTemplate().getTemplateId());

		long count = item.getItemCount();
		int maxStackCount = item.getItemTemplate().getMaxStackCount();

		for(Item existingItem : existingItems)
		{
			if(count == 0)
				break;

			long freeCount = maxStackCount - existingItem.getItemCount();
			if(count <= freeCount)
			{
				destinationStorage.increaseItemCount(existingItem, count);
				count = 0;
				sendDeleteItemPacket(player, sourceStorageType, item.getObjectId());
				sourceStorage.removeFromBag(item, true);

			}
			else
			{
				destinationStorage.increaseItemCount(existingItem, freeCount);
				count -= freeCount;
			}
			sendStorageUpdatePacket(player, destinationStorageType, existingItem);

		}

		while(!destinationStorage.isFull() && count > 0)
		{
			// item count still more than maxStack value
			if(count > maxStackCount)
			{
				count -= maxStackCount;
				Item newitem = newItem(item.getItemTemplate().getTemplateId(), maxStackCount);
				newitem = destinationStorage.putToBag(newitem);
				sendStorageUpdatePacket(player, destinationStorageType, newitem);
			}
			else
			{
				item.setItemCount(count);
				sourceStorage.removeFromBag(item, false);
				sendDeleteItemPacket(player, sourceStorageType, item.getObjectId());
				Item newitem = destinationStorage.putToBag(item);
				sendStorageUpdatePacket(player, destinationStorageType, newitem);

				count = 0;
			}
		}

		if(count > 0) // if storage is full and some items left
		{
			item.setItemCount(count);
			sendUpdateItemPacket(player, sourceStorageType, item);
		}

	}


	public static void updateItem(Player player, Item item, boolean isNew)
	{
		if(isNew)
			PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE(Collections.singletonList(item)));
		else
			PacketSendUtility.sendPacket(player, new SM_UPDATE_ITEM(item));
	}

	private static void sendDeleteItemPacket(Player player, int storageType, int itemObjId)
	{
		if(storageType == StorageType.CUBE.getId())
			PacketSendUtility.sendPacket(player, new SM_DELETE_ITEM(itemObjId));
		else
			PacketSendUtility.sendPacket(player, new SM_DELETE_WAREHOUSE_ITEM(storageType, itemObjId));
	}

	private static void sendStorageUpdatePacket(Player player, int storageType, Item item)
	{
		if(storageType == StorageType.CUBE.getId())
			PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE(Collections.singletonList(item)));
		else
			PacketSendUtility.sendPacket(player, new SM_WAREHOUSE_UPDATE(item, storageType));
	}

	private static void sendUpdateItemPacket(Player player, int storageType, Item item)
	{
		if(storageType == StorageType.CUBE.getId())
			PacketSendUtility.sendPacket(player, new SM_UPDATE_ITEM(item));
		else
			PacketSendUtility.sendPacket(player, new SM_UPDATE_WAREHOUSE_ITEM(item, storageType));
	}

	/**
	 *  Releases item id if item was not used by caller
	 *  
	 * @param item
	 */
	public static void releaseItemId(Item item)
	{
		IDFactory.getInstance().releaseId(item.getObjectId());
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
		
		//temp fix for manastone spam till templates are updated
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

		ManaStone stone = new ManaStone(item.getObjectId(), itemId,
			nextSlot, PersistentState.NEW);
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
		Item kinahItem = player.getInventory().getKinahItem();
		if(kinahItem.getItemCount() < socketPrice)
			return;
		
		Item weaponItem = player.getInventory().getItemByObjId(weaponId);
		if ( weaponItem == null)
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_PROC_CANNOT_GIVE_PROC_TO_EQUIPPED_ITEM);
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
		
		weaponItem.addGodStone(godStoneItemId);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_PROC_ENCHANTED_TARGET_ITEM(new DescriptionId(Integer.parseInt(weaponItem.getName()))));
		player.getInventory().removeFromBagByObjectId(stoneId, 1);
		
		player.getInventory().decreaseKinah(socketPrice);
		PacketSendUtility.sendPacket(player, new SM_UPDATE_ITEM(weaponItem));
		PacketSendUtility.sendPacket(player, new SM_UPDATE_ITEM(kinahItem));
	}
	
	public static boolean addItems(Player player, List<QuestItems> questItems)
	{
		int needSlot = 0;
		for (QuestItems qi : questItems)
		{
			if (qi.getItemId() != ItemId.KINAH.value() && qi.getCount()!= 0)
			{
				int stackCount = DataManager.ITEM_DATA.getItemTemplate(qi.getItemId()).getMaxStackCount();
				int count = qi.getCount()/stackCount;
				if (qi.getCount() % stackCount != 0)
					count++;
				needSlot += count;
			}
		}
		if (needSlot > player.getInventory().getNumberOfFreeSlots())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.MSG_FULL_INVENTORY);
			return false;
		}
		for (QuestItems qi : questItems)
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
			player.getStorage(StorageType.CUBE.getId()).onLoadHandler(kinahItem);
		}

		if(player.getStorage(StorageType.ACCOUNT_WAREHOUSE.getId()).getKinahItem() == null)
		{
			Item kinahItem = newItem(182400001, 0);
			kinahItem.setItemLocation(StorageType.ACCOUNT_WAREHOUSE.getId());
			player.getStorage(StorageType.ACCOUNT_WAREHOUSE.getId()).onLoadHandler(kinahItem);
		}
	}
}
