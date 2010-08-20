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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.trade.Exchange;
import com.aionemu.gameserver.model.trade.ExchangeItem;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EXCHANGE_ADD_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EXCHANGE_ADD_KINAH;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EXCHANGE_CONFIRMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EXCHANGE_REQUEST;
import com.aionemu.gameserver.restrictions.RestrictionsManager;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 *
 */
public class ExchangeService
{
	private static final Logger log = Logger.getLogger(ExchangeService.class);

	private Map<Integer, Exchange>		exchanges;
	
	public static final ExchangeService getInstance()
	{
		return SingletonHolder.instance;
	}

	/**
	 * Default constructor
	 */
	private ExchangeService()
	{
		exchanges			= new HashMap<Integer, Exchange>();
		log.info("ExchangeService: Initialized.");
	}

	/**
	 * @param objectId
	 * @param objectId2
	 */
	public void registerExchange(Player player1, Player player2)
	{
		if(!validateParticipants(player1, player2))
			return;
		
		player1.setTrading(true);
		player2.setTrading(true);
		
		exchanges.put(player1.getObjectId(), new Exchange(player1, player2));
		exchanges.put(player2.getObjectId(), new Exchange(player2, player1));

		PacketSendUtility.sendPacket(player2, new SM_EXCHANGE_REQUEST(player1.getName()));
		PacketSendUtility.sendPacket(player1, new SM_EXCHANGE_REQUEST(player2.getName()));
	}

	/**
	 * @param player1
	 * @param player2
	 */
	private boolean validateParticipants(Player player1, Player player2)
	{
		return RestrictionsManager.canTrade(player1) && RestrictionsManager.canTrade(player2);
	}

	private Player getCurrentParter(Player player)
	{
		Exchange exchange = exchanges.get(player.getObjectId());
		return exchange != null ? exchange.getTargetPlayer() : null;
	}
	/**
	 * 
	 * @param player
	 * @return Exchange
	 */
	private Exchange getCurrentExchange(Player player)
	{
		return exchanges.get(player.getObjectId());
	}

	/**
	 * 
	 * @param player
	 * @return Exchange
	 */
	public Exchange getCurrentParnterExchange(Player player)
	{
		Player partner = getCurrentParter(player);
		return partner != null ? getCurrentExchange(partner) : null;
	}

	/**
	 * @param activePlayer
	 * @param itemCount
	 */
	public void addKinah(Player activePlayer, long itemCount)
	{
		Exchange currentExchange = getCurrentExchange(activePlayer);
		if(currentExchange.isLocked())
			return;
		
		if(itemCount < 1)
			return;

		//count total amount in inventory
		long availableCount = activePlayer.getInventory().getKinahItem().getItemCount();
		
		//count amount that was already added to exchange
		availableCount -= currentExchange.getKinahCount();

		long countToAdd = availableCount > itemCount ? itemCount : availableCount;

		if(countToAdd > 0)
		{
			Player partner = getCurrentParter(activePlayer);
			PacketSendUtility.sendPacket(activePlayer, new SM_EXCHANGE_ADD_KINAH(countToAdd, 0));
			PacketSendUtility.sendPacket(partner, new SM_EXCHANGE_ADD_KINAH(countToAdd, 1));
			currentExchange.addKinah(countToAdd);
		}
	}

	/**
	 * @param activePlayer
	 * @param itemObjId
	 * @param itemCount
	 */
	public void addItem(Player activePlayer, int itemObjId, long itemCount)
	{
		Item item = activePlayer.getInventory().getItemByObjId(itemObjId);
		if(item == null)
			return;
		
		// Check Trade Hack
		if (!item.isTradeable())
			return;
			
		if(itemCount < 1)
			return;
		
		if(itemCount > item.getItemCount())
			return;

		Player partner = getCurrentParter(activePlayer);
		Exchange currentExchange = getCurrentExchange(activePlayer);

		if (currentExchange == null)
			return;

		if(currentExchange.isLocked())
			return;

		if(currentExchange.isExchangeListFull())
			return;

		ExchangeItem exchangeItem = currentExchange.getItems().get(item.getObjectId());

		long actuallAddCount = 0;
		//item was not added previosly
		if(exchangeItem == null)
		{
			if (item.getItemCount() == itemCount)
				exchangeItem = new ExchangeItem(itemObjId, itemCount, item);
			else
			{
				Item newItem = ItemService.newItem(item.getItemId(), itemCount);
				newItem.setPersistentState(PersistentState.NOACTION);
				exchangeItem = new ExchangeItem(itemObjId, itemCount, newItem);
			}
			currentExchange.addItem(itemObjId, exchangeItem);
			actuallAddCount = itemCount;
		}
		//item was already added
		else
		{
			//if player add item count that is more than possible
			//happens with exploits
			if(item.getItemCount() == exchangeItem.getItemCount())
				return;

			long possibleToAdd = item.getItemCount() - exchangeItem.getItemCount();
			actuallAddCount = itemCount > possibleToAdd ? possibleToAdd : itemCount;	
			exchangeItem.addCount(actuallAddCount);
		}

		PacketSendUtility.sendPacket(activePlayer, new SM_EXCHANGE_ADD_ITEM(0, exchangeItem.getItem()));
		PacketSendUtility.sendPacket(partner, new SM_EXCHANGE_ADD_ITEM(1, exchangeItem.getItem()));
	}

	/**
	 * @param activePlayer
	 */
	public void lockExchange(Player activePlayer)
	{
		Exchange exchange = getCurrentExchange(activePlayer);
		if(exchange != null)
		{
			exchange.lock();
			Player currentParter = getCurrentParter(activePlayer);
			PacketSendUtility.sendPacket(currentParter, new SM_EXCHANGE_CONFIRMATION(3));
		}
	}

	/**
	 * @param activePlayer
	 */
	public void cancelExchange(Player activePlayer)
	{
		Player currentParter = getCurrentParter(activePlayer);	
		cleanupExchanges(activePlayer, currentParter);	
		if(currentParter != null)
			PacketSendUtility.sendPacket(currentParter, new SM_EXCHANGE_CONFIRMATION(1));
	}

	/**
	 * @param activePlayer
	 */
	public void confirmExchange(Player activePlayer)
	{
		Exchange currentExchange = getCurrentExchange(activePlayer);
		currentExchange.confirm();

		Player currentPartner = getCurrentParter(activePlayer);	
		PacketSendUtility.sendPacket(currentPartner, new SM_EXCHANGE_CONFIRMATION(2));

		if(getCurrentExchange(currentPartner).isConfirmed())
		{
			performTrade(activePlayer, currentPartner);
		}
	}

	/**
	 * @param activePlayer
	 * @param currentPartner
	 */
	private void performTrade(Player activePlayer, Player currentPartner)
	{
		//TODO message here
		//TODO release item id if return
		if(!validateExchange(activePlayer, currentPartner))
			return;

		PacketSendUtility.sendPacket(activePlayer, new SM_EXCHANGE_CONFIRMATION(0));
		PacketSendUtility.sendPacket(currentPartner, new SM_EXCHANGE_CONFIRMATION(0));
		
		doExchanges(activePlayer, currentPartner);
		doExchanges(currentPartner, activePlayer);

		cleanupExchanges(activePlayer, currentPartner);
	}

	/**
	 * 
	 * @param activePlayer
	 * @param currentPartner
	 */
	private void cleanupExchanges(Player activePlayer, Player currentPartner)
	{
		if(activePlayer != null)
		{
			Exchange exchange = exchanges.remove(activePlayer.getObjectId());
			if (exchange != null)
				exchange.clear();
			activePlayer.setTrading(false);
		}
			
		if(currentPartner != null)
		{
			Exchange exchange = exchanges.remove(currentPartner.getObjectId());
			if (exchange != null)
				exchange.clear();
			currentPartner.setTrading(false);
		}
	}

	private void doExchanges(Player sourcePlayer, Player targetPlayer)
	{
		Exchange exchange = getCurrentExchange(sourcePlayer);
		
		for(ExchangeItem exchangeItem : exchange.getItems().values())
		{
			Item itemInInventory = sourcePlayer.getInventory().getItemByObjId(exchangeItem.getItemObjId());
			if (exchangeItem.getItemCount() == itemInInventory.getItemCount())
			{
				ItemService.removeItem(sourcePlayer, sourcePlayer.getInventory(), itemInInventory, false);
				ItemService.addFullItem(targetPlayer, targetPlayer.getInventory(), itemInInventory);
			}
			else
			{
				ItemService.decreaseItemCount(sourcePlayer, itemInInventory, exchangeItem.getItemCount());
				ItemService.addItem(targetPlayer, itemInInventory.getItemId(), exchangeItem.getItemCount());
			}
		}
		long kinah = exchange.getKinahCount();
		if (kinah > 0)
		{
			ItemService.decreaseKinah(sourcePlayer, exchange.getKinahCount());
			ItemService.increaseKinah(targetPlayer, exchange.getKinahCount());
		}
	}

	/**
	 * @param activePlayer
	 * @param currentPartner
	 * @return
	 */
	private boolean validateExchange(Player activePlayer, Player currentPartner)
	{
		Exchange exchange1 = getCurrentExchange(activePlayer);
		Exchange exchange2 = getCurrentExchange(currentPartner);

		return validateInventorySize(activePlayer, exchange2) 
		&& validateInventorySize(currentPartner, exchange1);
	}

	private boolean validateInventorySize(Player activePlayer, Exchange exchange)
	{
		int numberOfFreeSlots = activePlayer.getInventory().getNumberOfFreeSlots();
		return numberOfFreeSlots >=  exchange.getItems().size();			
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ExchangeService instance = new ExchangeService();
	}
}
