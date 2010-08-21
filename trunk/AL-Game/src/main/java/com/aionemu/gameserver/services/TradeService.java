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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.GoodsListData;
import com.aionemu.gameserver.dataholders.TradeListData;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Storage;
import com.aionemu.gameserver.model.templates.TradeListTemplate;
import com.aionemu.gameserver.model.templates.TradeListTemplate.TradeTab;
import com.aionemu.gameserver.model.templates.goods.GoodsList;
import com.aionemu.gameserver.model.trade.TradeItem;
import com.aionemu.gameserver.model.trade.TradeList;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer, Rama
 * 
 */
public class TradeService
{
	private static final Logger			log				= Logger.getLogger(TradeService.class);

	private static final TradeListData	tradeListData	= DataManager.TRADE_LIST_DATA;
	private static final GoodsListData	goodsListData	= DataManager.GOODSLIST_DATA;

	/**
	 * 
	 * @param player
	 * @param tradeList
	 * @return true or false
	 */
	public static boolean performBuyFromShop(Player player, TradeList tradeList)
	{

		if(!validateBuyItems(tradeList, player))
		{
			PacketSendUtility.sendMessage(player, "Some items are not allowed to be sold by this npc.");
			return false;
		}

		Storage inventory = player.getInventory();

		Npc npc = (Npc) World.getInstance().findAionObject(tradeList.getSellerObjId());
		int tradeModifier = tradeListData.getTradeListTemplate(npc.getNpcId()).getSellPriceRate();

		// 1. check kinah
		if(!tradeList.calculateBuyListPrice(player, tradeModifier))
			return false;

		// 2. check free slots, need to check retail behaviour
		int freeSlots = inventory.getLimit() - inventory.getAllItems().size() + 1;
		if(freeSlots < tradeList.size())
			return false; // TODO message

		long tradeListPrice = tradeList.getRequiredKinah();

		for(TradeItem tradeItem : tradeList.getTradeItems())
		{
			if(!ItemService.addItem(player, tradeItem.getItemTemplate().getTemplateId(), tradeItem.getCount()))
			{
				log.warn(String.format("CHECKPOINT: itemservice couldnt add all items on buy: %d %d %d %d", player
					.getObjectId(), tradeItem.getItemTemplate().getTemplateId(), tradeItem.getCount(), tradeItem
					.getCount()));
				ItemService.decreaseKinah(player, tradeListPrice);
				return false;
			}
		}
		ItemService.decreaseKinah(player, tradeListPrice);
		// TODO message
		return true;
	}

	/**
	 * Probably later merge with regular buy
	 * 
	 * @param player
	 * @param tradeList
	 * @return true or false
	 */
	public static boolean performBuyFromAbyssShop(Player player, TradeList tradeList)
	{

		if(!validateBuyItems(tradeList, player))
		{
			PacketSendUtility.sendMessage(player, "Some items are not allowed to be selled from this npc");
			return false;
		}
		Storage inventory = player.getInventory();
		int freeSlots = inventory.getLimit() - inventory.getAllItems().size() + 1;

		// 1. check required items and ap
		if(!tradeList.calculateAbyssBuyListPrice(player))
			return false;

		// 2. check free slots, need to check retail behaviour
		if(freeSlots < tradeList.size())
			return false; // TODO message

		for(TradeItem tradeItem : tradeList.getTradeItems())
		{
			if(!ItemService.addItem(player, tradeItem.getItemTemplate().getTemplateId(), tradeItem.getCount()))
			{
				log.warn(String.format("CHECKPOINT: itemservice couldnt add all items on buy: %d %d %d %d", player
					.getObjectId(), tradeItem.getItemTemplate().getTemplateId(), tradeItem.getCount(), tradeItem
					.getCount()));
				player.getCommonData().addAp(-tradeList.getRequiredAp());
				return false;
			}
		}

		player.getCommonData().addAp(-tradeList.getRequiredAp());
		Map<Integer, Integer> requiredItems = tradeList.getRequiredItems();
		for(Integer itemId : requiredItems.keySet())
		{
			ItemService.decreaseItemCountByItemId(player, itemId, requiredItems.get(itemId));
		}

		// TODO message
		return true;
	}

	/**
	 * @param tradeList
	 */
	private static boolean validateBuyItems(TradeList tradeList, Player player)
	{
		Npc npc = (Npc) World.getInstance().findAionObject(tradeList.getSellerObjId());
		TradeListTemplate tradeListTemplate = tradeListData.getTradeListTemplate(npc.getObjectTemplate()
			.getTemplateId());

		Set<Integer> allowedItems = new HashSet<Integer>();
		for(TradeTab tradeTab : tradeListTemplate.getTradeTablist())
		{
			GoodsList goodsList = goodsListData.getGoodsListById(tradeTab.getId());
			if(goodsList != null && goodsList.getItemIdList() != null)
			{
				allowedItems.addAll(goodsList.getItemIdList());
			}
		}

		for(TradeItem tradeItem : tradeList.getTradeItems())
		{
			if(tradeItem.getCount() < 1)
			{
				log.warn("[AUDIT] Player: " + player.getName() + " posible client hack. Trade count < 1");
				return false;
			}
			if(!allowedItems.contains(tradeItem.getItemId()))
			{
				log.warn("[AUDIT] Player: " + player.getName() + " posible client hack. Tade item not in GoodsList");
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param player
	 * @param tradeList
	 * @return true or false
	 */
	public static boolean performSellToShop(Player player, TradeList tradeList)
	{
		Storage inventory = player.getInventory();

		long kinahReward = 0;
		for(TradeItem tradeItem : tradeList.getTradeItems())
		{
			Item item = inventory.getItemByObjId(tradeItem.getItemId());
			// 1) don't allow to sell fake items;
			if(item == null)
				return false;

			if(item.getItemCount() - tradeItem.getCount() < 0)
			{
				log.warn("[AUDIT] Trade exploit, sell item count big: " + player.getName());
				return false;
			}
			else if(ItemService.decreaseItemCount(player, item, tradeItem.getCount()) == 0)
			{
				// TODO check retail packet here
				kinahReward += item.getItemTemplate().getPrice() * tradeItem.getCount();
			}
		}

		kinahReward = player.getPrices().getKinahForSell(kinahReward);
		ItemService.increaseKinah(player, kinahReward);

		return true;
	}

	/**
	 * @return the tradeListData
	 */
	public static TradeListData getTradeListData()
	{
		return tradeListData;
	}

}
