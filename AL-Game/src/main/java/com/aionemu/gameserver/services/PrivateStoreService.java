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
package com.aionemu.gameserver.services;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PrivateStore;
import com.aionemu.gameserver.model.gameobjects.player.Storage;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.trade.TradeItem;
import com.aionemu.gameserver.model.trade.TradeList;
import com.aionemu.gameserver.model.trade.TradePSItem;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PRIVATE_STORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PRIVATE_STORE_NAME;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Simple
 * 
 */
public class PrivateStoreService
{

	/**
	 * @param activePlayer
	 * @param itemObjId
	 * @param itemId
	 * @param itemAmount
	 * @param itemPrice
	 */
	public static void addItem(Player activePlayer, TradePSItem[] tradePSItems)
	{
		/**
		 * Check if player already has a store, if not create one
		 */
		if(activePlayer.getStore() == null)
			createStore(activePlayer);

		/**
		 * Define store to make things easier
		 */
		PrivateStore store = activePlayer.getStore();

		/**
		 * Check if player owns itemObjId else don't add item
		 */
		for(int i = 0; i < tradePSItems.length; i++)
		{
			Item item = getItemByObjId(activePlayer, tradePSItems[i].getItemObjId());
			if(item != null && item.isTradeable())
			{
				if(!validateItem(item, tradePSItems[i].getItemId(), tradePSItems[i].getCount()))
					return;
				/**
				 * Add item to private store
				 */
				store.addItemToSell(tradePSItems[i].getItemObjId(), tradePSItems[i]);
			}
		}
	}

	/**
	 * A check isn't really needed.....
	 * 
	 * @return
	 */
	private static boolean validateItem(Item item, int itemId, long itemAmount)
	{
		return !(item.getItemTemplate().getTemplateId() != itemId || itemAmount > item.getItemCount());
	}

	/**
	 * This method will create the player's store
	 * 
	 * @param activePlayer
	 */
	private static void createStore(Player activePlayer)
	{
		activePlayer.setStore(new PrivateStore(activePlayer));
		activePlayer.setState(CreatureState.PRIVATE_SHOP);
		PacketSendUtility.broadcastPacket(activePlayer,
			new SM_EMOTION(activePlayer, EmotionType.OPEN_PRIVATESHOP, 0, 0), true);
	}

	/**
	 * This method will destroy the player's store
	 * 
	 * @param activePlayer
	 */
	public static void closePrivateStore(Player activePlayer)
	{
		activePlayer.setStore(null);
		activePlayer.unsetState(CreatureState.PRIVATE_SHOP);
		PacketSendUtility.broadcastPacket(activePlayer, new SM_EMOTION(activePlayer, EmotionType.CLOSE_PRIVATESHOP, 0,
			0), true);
	}

	/**
	 * This method will move the item to the new player and move kinah to item owner
	 */
	public static void sellStoreItem(Player seller, Player buyer, TradeList tradeList)
	{
		/**
		 * 1. Check if we are busy with two valid participants
		 */
		if(!validateParticipants(seller, buyer))
			return;

		/**
		 * Define store to make life easier
		 */
		PrivateStore store = seller.getStore();

		/**
		 * 2. Load all item object id's and validate if seller really owns them
		 */
		tradeList = loadObjIds(seller, tradeList);
		if(tradeList == null)
			return; // Invalid items found or store was empty

		/**
		 * 3. Check free slots
		 */
		Storage inventory = buyer.getInventory();
		int freeSlots = inventory.getLimit() - inventory.getAllItems().size() + 1;
		if(freeSlots < tradeList.size())
			return; // TODO message

		/**
		 * Create total price and items
		 */
		long price = getTotalPrice(store, tradeList);

		/**
		 * Check if player has enough kinah and remove it
		 */
		if(!ItemService.decreaseKinah(buyer, price))
			return;
		/**
		 * Increase kinah for seller
		 */
		ItemService.increaseKinah(seller, price);

		for(TradeItem tradeItem : tradeList.getTradeItems())
		{
			Item item = getItemByObjId(seller, tradeItem.getItemId());
			if(item != null)
			{
				TradePSItem storeItem = store.getTradeItemById(tradeItem.getItemId());
				if(item.getItemCount() == tradeItem.getCount())
				{
					ItemService.removeItem(seller, seller.getInventory(), item, false);
					ItemService.addFullItem(buyer, buyer.getInventory(), item);
					store.removeItem(storeItem.getItemObjId());
				}
				else
				{
					ItemService.decreaseItemCount(seller, item, tradeItem.getCount());
					ItemService.addItem(buyer, item.getItemId(), tradeItem.getCount());
					store.getTradeItemById(storeItem.getItemObjId()).decreaseCount(tradeItem.getCount());
					if(store.getTradeItemById(storeItem.getItemObjId()).getCount() == 0)
						store.removeItem(storeItem.getItemObjId());
				}
			}
		}

		/**
		 * Remove item from store and check if last item
		 */
		if(store.getSoldItems().size() == 0)
			closePrivateStore(seller);
		else
			PacketSendUtility.sendPacket(buyer, new SM_PRIVATE_STORE(store));
		return;
	}

	/**
	 * @param seller
	 * @param tradeList
	 * @return
	 */
	private static TradeList loadObjIds(Player seller, TradeList tradeList)
	{
		PrivateStore store = seller.getStore();
		TradeList newTradeList = new TradeList();

		for(TradeItem tradeItem : tradeList.getTradeItems())
		{
			int i = 0; 
			for(int itemObjId : store.getSoldItems().keySet()) 
			{ 
				if(i == tradeItem.getItemId()) 
					newTradeList.addPSItem(itemObjId, tradeItem.getCount()); 
				i++; 
			} 
		}

		/**
		 * Check if player still owns items
		 */
		if(newTradeList.size() == 0 || !validateBuyItems(seller, newTradeList))
			return null;

		return newTradeList;
	}

	/**
	 * @param player1
	 * @param player2
	 */
	private static boolean validateParticipants(Player itemOwner, Player newOwner)
	{
		return itemOwner != null && newOwner != null && itemOwner.isOnline() && newOwner.isOnline();
	}

	/**
	 * @param tradeList
	 */
	private static boolean validateBuyItems(Player seller, TradeList tradeList)
	{
		for(TradeItem tradeItem : tradeList.getTradeItems())
		{
			Item item = seller.getInventory().getItemByObjId(tradeItem.getItemId());

			// 1) don't allow to sell fake items;
			if(item == null)
				return false;
			
		}
		return true;
	}

	/**
	 * This method will return the item in a inventory by object id
	 * 
	 * @param player
	 * @param tradePSItems
	 * @return
	 */
	private static Item getItemByObjId(Player seller, int itemObjId)
	{
		return seller.getInventory().getItemByObjId(itemObjId);
	}

	/**
	 * This method will return the total price of the tradelist
	 * 
	 * @param store
	 * @param tradeList
	 * @return
	 */
	private static long getTotalPrice(PrivateStore store, TradeList tradeList)
	{
		long totalprice = 0;
		for(TradeItem tradeItem : tradeList.getTradeItems())
		{
			TradePSItem item = store.getTradeItemById(tradeItem.getItemId());
			totalprice += item.getPrice() * tradeItem.getCount();
		}
		return totalprice;
	}

	/**
	 * @param activePlayer
	 */
	public static void openPrivateStore(Player activePlayer, String name)
	{
		if(name != null)
		{
			activePlayer.getStore().setStoreMessage(name);
			PacketSendUtility.broadcastPacket(activePlayer,
				new SM_PRIVATE_STORE_NAME(activePlayer.getObjectId(), name), true);
		}
		else
		{
			PacketSendUtility.broadcastPacket(activePlayer, new SM_PRIVATE_STORE_NAME(activePlayer.getObjectId(), ""),
				true);
		}
	}
}
