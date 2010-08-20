/*
 * This file is part of aion-unique <aion-unique.org>.
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
package com.aionemu.gameserver.model.gameobjects.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.items.ItemStorage;

/**
 * @author Avol
 * modified by ATracer, kosyachok
 */
public class Storage
{
	
	private int ownerId;

	protected ItemStorage storage;

	private Item kinahItem;

	protected int storageType;
	
	protected Queue<Item> deletedItems = new ConcurrentLinkedQueue<Item>();
	
	/**
	 * Can be of 2 types: UPDATED and UPDATE_REQUIRED
	 */
	private PersistentState persistentState = PersistentState.UPDATED;

	/**
	 *  Will be enhanced during development.
	 */
	public Storage(StorageType storageType)
	{
		switch(storageType)
		{
			case CUBE:
				storage = new ItemStorage(109);
				this.storageType = storageType.getId();
				break;
			case REGULAR_WAREHOUSE:
				storage = new ItemStorage(104);
				this.storageType = storageType.getId();
				break;
			case ACCOUNT_WAREHOUSE:
				storage = new ItemStorage(17);
				this.storageType = storageType.getId();
				break;
			case LEGION_WAREHOUSE:
				storage = new ItemStorage(25); // TODO: FIND OUT WHAT MAX IS
				this.storageType = storageType.getId();
				break;
		}
	}

	/**
	 * @return the ownerId
	 */
	public int getOwnerId()
	{
		return ownerId;
	}

	/**
	 * @param ownerId the ownerId to set
	 */
	public void setOwnerId(int ownerId)
	{
		this.ownerId = ownerId;
	}

	/**
	 * @return the kinahItem
	 */
	public Item getKinahItem()
	{
		return kinahItem;
	}

	/**
	 * @param kinahItem the kinahItem to set
	 */
	public void setKinahItem(Item kinahItem)
	{
		this.kinahItem = kinahItem;
	}

	public int getStorageType()
	{
		return storageType;
	}
	/**
	 *  Increasing kinah amount is persisted immediately
	 *  
	 * @param amount
	 */
	public void increaseKinah(long amount)
	{
		kinahItem.increaseItemCount(amount);
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}
	/**
	 *  Decreasing kinah amount is persisted immediately
	 *  
	 * @param amount
	 */
	public boolean decreaseKinah(long amount)
	{
		boolean operationResult = kinahItem.decreaseItemCount(amount);
		setPersistentState(PersistentState.UPDATE_REQUIRED);
		return operationResult;
	}

	/**
	 *  Used to put item into storage cube at first avaialble slot (no check for existing item)
	 *  During unequip/equip process persistImmediately should be false
	 *  
	 * @param item
	 * @param persistImmediately
	 * @return Item
	 */
	public Item putToBag(Item item)
	{

		Item resultItem = storage.putToNextAvailableSlot(item);
		if(resultItem != null)
		{
			resultItem.setItemLocation(storageType);
		}
		item.setOwnerId(ownerId);
		setPersistentState(PersistentState.UPDATE_REQUIRED);
		return resultItem;
	}

	/**
	 *  Removes item completely from inventory.
	 *  Every remove operation is persisted immediately now
	 *  
	 * @param item
	 */
	public void removeFromBag(Item item, boolean persist)
	{
		boolean operationResult = storage.removeItemFromStorage(item);
		if(operationResult && persist)
		{
			item.setPersistentState(PersistentState.DELETED);
			deletedItems.add(item);
			setPersistentState(PersistentState.UPDATE_REQUIRED);
		}
	}

	/**
	 * 
	 * @param itemId
	 * @return Item
	 */
	public Item getFirstItemByItemId(int itemId)
	{
		List<Item> items = storage.getItemsFromStorageByItemId(itemId);
		if (items.size() == 0)
			return null;
		return items.get(0);
	}

	/**
	 *  Method primarily used when saving to DB
	 *  
	 * @return List<Item>
	 */
	public List<Item> getAllItems()
	{
		List<Item> allItems = new ArrayList<Item>();
		if(kinahItem != null)
			allItems.add(kinahItem);
		allItems.addAll(storage.getStorageItems());
		return allItems;
	}
	
	/**
	 *  All deleted items with persistent state DELETED
	 *  
	 * @return
	 */
	public Queue<Item> getDeletedItems()
	{
		return deletedItems;
	}

	/**
	 *  Searches for item with specified itemId in equipment and cube
	 *  
	 * @param itemId
	 * @return List<Item>
	 */
	public List<Item> getAllItemsByItemId(int itemId)
	{
		List<Item> allItemsByItemId = new ArrayList<Item>();

		for (Item item : storage.getStorageItems())
		{
			if(item.getItemTemplate().getTemplateId() == itemId)
				allItemsByItemId.add(item);
		}
		return allItemsByItemId;
	}


	public List<Item> getStorageItems()
	{
		return storage.getStorageItems();
	}

	/**
	 *  Will look item in default item bag
	 *  
	 * @param value
	 * @return Item
	 */
	public Item getItemByObjId(int value)
	{
		return storage.getItemFromStorageByItemObjId(value);
	}

	/**
	 * 
	 * @param value
	 * @return List<Item>
	 */
	public List<Item> getItemsByItemId(int value)
	{
		return storage.getItemsFromStorageByItemId(value);
	}

	/**
	 *  
	 * @param itemId
	 * @return number of items using search by itemid
	 */
	public long getItemCountByItemId(int itemId)
	{
		List<Item> items = getItemsByItemId(itemId);
		long count = 0;
		for(Item item : items)
		{
			count += item.getItemCount();
		}
		return count;
	}

	/**
	 *  Checks whether default cube is full
	 *  
	 * @return true or false
	 */
	public boolean isFull()
	{
		return storage.isFull();
	}
	
	/**
	 *  Number of available slots of the underlying storage
	 *  
	 * @return
	 */
	public int getNumberOfFreeSlots()
	{
		return storage.getNumberOfFreeSlots();
	}
	
	/**
	 *  Sets the Inventory Limit from Cube Size
	 *  
	 * @param Limit
	 */
	public void setLimit(int limit)
	{
		this.storage.setLimit(limit);
	}
	
	/**
	 *  Limit value of the underlying storage
	 *  
	 * @return
	 */
	public int getLimit()
	{
		return this.storage.getLimit();
	}

	/**
	 * @return the persistentState
	 */
	public PersistentState getPersistentState()
	{
		return persistentState;
	}

	/**
	 * @param persistentState the persistentState to set
	 */
	public void setPersistentState(PersistentState persistentState)
	{
		this.persistentState = persistentState;
	}

	/**
	 * @param item
	 * @param count
	 */
	public void increaseItemCount(Item item, long count)
	{
		item.increaseItemCount(count);
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public Item putToNextAvailableSlot(Item item)
	{
		return storage.putToNextAvailableSlot(item);
	}
	/**
	 *  Size of underlying storage
	 *  
	 * @return
	 */
	public int size()
	{
		return storage.size();
	}
}
