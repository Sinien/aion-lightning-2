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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dao.DropListDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.alliance.PlayerAllianceMember;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.drop.DropList;
import com.aionemu.gameserver.model.drop.DropTemplate;
import com.aionemu.gameserver.model.gameobjects.DropNpc;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_LOOT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LOOT_ITEMLIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LOOT_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.stats.DropRewardEnum;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer
 */
public class DropService
{
	private static final Logger			log					= Logger.getLogger(DropService.class);

	private DropList					dropList;

	private Map<Integer, Set<DropItem>>	currentDropMap		= new FastMap<Integer, Set<DropItem>>().shared();
	private Map<Integer, DropNpc>		dropRegistrationMap	= new FastMap<Integer, DropNpc>().shared();

	public static final DropService getInstance()
	{
		return SingletonHolder.instance;
	}

	private DropService()
	{
		dropList = DAOManager.getDAO(DropListDAO.class).load();
		log.info(dropList.getSize() + " npc drops loaded");
	}

	/**
	 * @return the dropList
	 */
	public DropList getDropList()
	{
		return dropList;
	}

	/**
	 * After NPC dies - it can register arbitrary drop
	 * 
	 * @param npc
	 */
	public void registerDrop(Npc npc, Player player, int lvl)
	{
		int npcUniqueId = npc.getObjectId();
		int npcTemplateId = npc.getObjectTemplate().getTemplateId();
		
		Set<DropItem> droppedItems = new HashSet<DropItem>();
		Set<DropTemplate> templates = dropList.getDropsFor(npcTemplateId);
		
		int index = 1;
		
		int dropPercentage = 100;
		if(!CustomConfig.DISABLE_DROP_REDUCTION)
			dropPercentage = DropRewardEnum.dropRewardFrom(npc.getLevel() - lvl);
		
		float dropRate = player.getRates().getDropRate() * dropPercentage / 100F;

		if(templates != null)
		{
			for(DropTemplate dropTemplate : templates)
			{
				DropItem dropItem = new DropItem(dropTemplate);
				dropItem.calculateCount(dropRate);

				if(dropItem.getCount() > 0)
				{
					dropItem.setIndex(index++);
					droppedItems.add(dropItem);
				}
			}
		}
		
		QuestService.getQuestDrop(droppedItems, index, npc, player);
		currentDropMap.put(npcUniqueId, droppedItems);

		// TODO: Player should not be null
		if(player == null) return;

		List<Player> dropPlayers = new ArrayList<Player>();
		if (player.isInAlliance())
		{
			// Register drop to all alliance members.
			List<Integer> dropMembers = new ArrayList<Integer>();
			for (PlayerAllianceMember member : player.getPlayerAlliance().getMembers())
			{
				dropMembers.add(member.getObjectId());
				dropPlayers.add(member.getPlayer());
			}
			dropRegistrationMap.put(npcUniqueId, new DropNpc(dropMembers));
			dropRegistrationMap.get(npcUniqueId).setGroupSize(player.getPlayerAlliance().size());			
		}
		else if (player.isInGroup())
		{
			dropRegistrationMap.put(npcUniqueId, new DropNpc(GroupService.getInstance().getMembersToRegistrateByRules(player,
				player.getPlayerGroup(), npc)));
			//Fetch players in range from GroupService
			DropNpc dropNpc = dropRegistrationMap.get(npcUniqueId);
			dropNpc.setInRangePlayers(GroupService.getInstance().getInRangePlayers());
			dropNpc.setGroupSize(dropNpc.getInRangePlayers().size());
			for(Player member : player.getPlayerGroup().getMembers())
			{
				if(dropNpc.containsKey(member.getObjectId()))
					dropPlayers.add(member);
			}
		}
		else
		{
			List<Integer> singlePlayer = new ArrayList<Integer>();
			singlePlayer.add(player.getObjectId());
			dropPlayers.add(player);
			dropRegistrationMap.put(npcUniqueId, new DropNpc(singlePlayer));
		}

		for(Player p : dropPlayers)
		{
			PacketSendUtility.sendPacket(p, new SM_LOOT_STATUS(npcUniqueId, 0));
		}
	}

	/**
	 * After NPC respawns - drop should be unregistered //TODO more correct - on despawn
	 * 
	 * @param npc
	 */
	public void unregisterDrop(Npc npc)
	{
		int npcUniqueId = npc.getObjectId();
		currentDropMap.remove(npcUniqueId);
		if(dropRegistrationMap.containsKey(npcUniqueId))
		{
			dropRegistrationMap.remove(npcUniqueId);
		}
	}

	/**
	 * When player clicks on dead NPC to request drop list
	 * 
	 * @param player
	 * @param npcId
	 */
	public void requestDropList(Player player, int npcId)
	{
		if(player == null || !dropRegistrationMap.containsKey(npcId))
			return;

		DropNpc dropNpc = dropRegistrationMap.get(npcId);
		if(!dropNpc.containsKey(player.getObjectId()))
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_LOOT_NO_RIGHT());
			return;
		}

		if(dropNpc.isBeingLooted())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_LOOT_FAIL_ONLOOTING());
			return;
		}

		dropNpc.setBeingLooted(player);

		Set<DropItem> dropItems = currentDropMap.get(npcId);

		if(dropItems == null)
		{
			dropItems = Collections.emptySet();
		}

		PacketSendUtility.sendPacket(player, new SM_LOOT_ITEMLIST(npcId, dropItems, player));
		// PacketSendUtility.sendPacket(player, new SM_LOOT_STATUS(npcId, size > 0 ? size - 1 : size));
		PacketSendUtility.sendPacket(player, new SM_LOOT_STATUS(npcId, 2));
		player.unsetState(CreatureState.ACTIVE);
		player.setState(CreatureState.LOOTING);
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_LOOT, 0, npcId), true);
	}

	/**
	 * This method will change looted corpse to not in use
	 * @param player
	 * @param npcId
	 * @param close
	 */
	public void requestDropList(Player player, int npcId, boolean close)
	{
		if(!dropRegistrationMap.containsKey(npcId))
			return;

		DropNpc dropNpc = dropRegistrationMap.get(npcId);
		dropNpc.setBeingLooted(null);

		player.unsetState(CreatureState.LOOTING);
		player.setState(CreatureState.ACTIVE);
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_LOOT, 0, npcId), true);

		Set<DropItem> dropItems = currentDropMap.get(npcId);
		Npc npc = (Npc) World.getInstance().findAionObject(npcId);
		if(npc != null)
		{
			if(dropItems == null || dropItems.size() == 0)
			{
				npc.getController().onDespawn(true);
				return;
			}

			PacketSendUtility.broadcastPacket(npc, new SM_LOOT_STATUS(npcId, 0));
			dropNpc.setFreeLooting();
		}
	}

	public void requestDropItem(Player player, int npcId, int itemIndex)
	{
		Set<DropItem> dropItems = currentDropMap.get(npcId);
		
		DropNpc dropNpc = dropRegistrationMap.get(npcId);
		
		// drop was unregistered
		if(dropItems == null || dropNpc == null)
		{
			return;
		}

		// TODO prevent possible exploits

		DropItem requestedItem = null;

		synchronized(dropItems)
		{
			for(DropItem dropItem : dropItems)
			{
				if(dropItem.getIndex() == itemIndex)
				{
					requestedItem = dropItem;
					break;
				}
			}
		}

		if(requestedItem != null)
		{
			long currentDropItemCount = requestedItem.getCount();
			int itemId = requestedItem.getDropTemplate().getItemId();
			
			ItemQuality quality = DataManager.ITEM_DATA.getItemTemplate(itemId).getItemQuality();		

			if(!requestedItem.isDistributeItem() && !requestedItem.isFreeForAll())
			{	
				if(player.isInGroup())
				{
					if(dropNpc.getGroupSize() > 1)
						dropNpc.setDistributionType(player.getPlayerGroup().getLootGroupRules().getQualityRule(quality));
					else
						dropNpc.setDistributionType(0);
					
					if(dropNpc.getDistributionType() > 1 && !dropNpc.isInUse())
					{
						dropNpc.setCurrentIndex(itemIndex);
						for(Player member : dropNpc.getInRangePlayers())
						{
							if(member.isOnline())
							{	
								dropNpc.addPlayerStatus(member);
								PacketSendUtility.sendPacket(member, new SM_GROUP_LOOT(member.getPlayerGroup().getGroupId(), 
									itemId, npcId, dropNpc.getDistributionType()));
							}
						}
						dropNpc.isInUse(true);// Set inUse to TRUE to stop multiple instances of item roll
					}
				}
				if(player.isInAlliance())
				{
					dropNpc.setDistributionType(0);

					if(dropNpc.getDistributionType() > 1 && !dropNpc.isInUse())
					{
						dropNpc.setCurrentIndex(itemIndex);
						for(PlayerAllianceMember allianceMember : player.getPlayerAlliance().getMembers())
						{
							Player member = allianceMember.getPlayer();
							if(member.isOnline())
							{
								dropNpc.addPlayerStatus(member);
								PacketSendUtility.sendPacket(member, new SM_GROUP_LOOT(member.getPlayerAlliance().getPlayerAllianceGroupForMember(
									member.getObjectId()).getAllianceId(), itemId, npcId, dropNpc.getDistributionType()));
							}
						}
						dropNpc.isInUse(true);
					}
				}				
			}

			//If looting player not in Group/Alliance or distribution is set to NORMAL
			//or all party members have passed, making item FFA....
			if((!player.isInGroup() && !player.isInAlliance()) || (dropNpc.getDistributionType() == 0) 
				|| (requestedItem.isFreeForAll()))
			{
				if (ItemService.addItem(player, itemId, currentDropItemCount))
					currentDropItemCount = 0;
			}

			// handles distribution of item to correct player and messages accordingly
			if(requestedItem.isDistributeItem())				
			{
				dropNpc.isInUse(false);
				
				if(player != requestedItem.getWinningPlayer() && requestedItem.isItemWonNotCollected())
				{
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LOOT_ANOTHER_OWNER_ITEM());
					return;
				}				
				else if(requestedItem.getWinningPlayer().getInventory().isFull())
				{
					PacketSendUtility.sendPacket(requestedItem.getWinningPlayer(), SM_SYSTEM_MESSAGE.STR_MSG_DICE_INVEN_ERROR);
					requestedItem.isItemWonNotCollected(true);
					return;
				}
				
				if (ItemService.addItem(requestedItem.getWinningPlayer(), itemId, currentDropItemCount))
					currentDropItemCount = 0;
				
				switch(dropNpc.getDistributionType())
				{
					case 2:
						winningRollActions(requestedItem.getWinningPlayer(), itemId, npcId);
						break;
					case 3:
						winningBidActions(requestedItem.getWinningPlayer(), itemId, npcId, requestedItem.getHighestValue());
				}
			}
			
			if(currentDropItemCount == 0)
			{
				dropItems.remove(requestedItem);
			}
			else
			{
				// If player didn't got all item stack
				requestedItem.setCount(currentDropItemCount);
			}

			// show updated drop list
			resendDropList(dropNpc.getBeingLooted(), npcId, dropItems);			
		}
	}
	
	private void resendDropList(Player player, int npcId, Set<DropItem> dropItems)
	{
		if(dropItems.size() != 0)
		{
			PacketSendUtility.sendPacket(player, new SM_LOOT_ITEMLIST(npcId, dropItems, player));
		}
		else
		{
			PacketSendUtility.sendPacket(player, new SM_LOOT_STATUS(npcId, 3));
			player.unsetState(CreatureState.LOOTING);
			player.setState(CreatureState.ACTIVE);
			PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_LOOT, 0, npcId), true);
			Npc npc = (Npc) World.getInstance().findAionObject(npcId);
			if(npc != null)
			{
				npc.getController().onDespawn(true);
			}
		}
	}

	/**
	 * @param Called from CM_GROUP_LOOT to handle rolls
	 */
	public void handleRoll(Player player, int roll, int itemId, int npcId)
	{
		switch(roll)
		{
			case 0:
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DICE_GIVEUP_ME());
				if(player.isInGroup())
				{
					for(Player member : dropRegistrationMap.get(npcId).getInRangePlayers())
					{
						if(!player.equals(member))
							PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_DICE_GIVEUP_OTHER(player.getName()));
					}
				}
				if(player.isInAlliance())
				{
					for(PlayerAllianceMember allianceMember : player.getPlayerAlliance().getMembers())
					{
						Player member = allianceMember.getPlayer();
						if(!player.equals(member))
							PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_DICE_GIVEUP_OTHER(player.getName()));
					}
				}				
					distributeLoot(player, 0, itemId, npcId);
				break;
			case 1:
				int luck = Rnd.get(1, 100);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DICE_RESULT_ME(luck));
				if(player.isInGroup())
				{				
					for(Player member : dropRegistrationMap.get(npcId).getInRangePlayers())
					{
						if(!player.equals(member))
							PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_DICE_RESULT_OTHER(player.getName(), luck));
					}
				}
				if(player.isInAlliance())
				{				
					for(PlayerAllianceMember allianceMember : player.getPlayerAlliance().getMembers())
					{
						Player member = allianceMember.getPlayer();
						if(!player.equals(member))
							PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_DICE_RESULT_OTHER(player.getName(), luck));
					}
				}				
					distributeLoot(player, luck, itemId, npcId);
				break;
		}
	}
	
	/**
	 * @param Called from CM_GROUP_LOOT to handle bids
	 */	
	public void handleBid(Player player, long bid, int itemId, int npcId)
	{
		long kinahAmount = player.getInventory().getKinahItem().getItemCount(); 
		if(bid > 0)
		{
			if(kinahAmount < bid)
			{
				bid = 0;// Set BID to 0 if player has bid more KINAH then they have in inventory
			}
			distributeLoot(player, bid, itemId, npcId);	
		}
		else
			distributeLoot(player, 0, itemId, npcId);						
	}

	/**
	 * @param Checks all players have Rolled or Bid then Distributes items accordingly
	 */
	private void distributeLoot(Player player, long luckyPlayer, int itemId, int npcId)
	{
		DropNpc dropNpc = dropRegistrationMap.get(npcId);
		
		Set<DropItem> dropItems = currentDropMap.get(npcId);
		DropItem requestedItem = null;

		synchronized(dropItems)
		{
			for(DropItem dropItem : dropItems)
			{
				if(dropItem.getIndex() == dropNpc.getCurrentIndex())
				{
					requestedItem = dropItem;
					break;
				}
			}
		}

		//Removes player from ARRAY once they have rolled or bid
		if(dropNpc.containsPlayerStatus(player))
		{
			dropNpc.delPlayerStatus(player);
		}
		
		if (luckyPlayer > requestedItem.getHighestValue())
		{
			requestedItem.setHighestValue(luckyPlayer);
			requestedItem.setWinningPlayer(player);
		}

		if(dropNpc.getPlayerStatus().size() != 0)		
			return;
		
		//Check if there is a Winning Player registered if not all members must have passed...
		if(requestedItem.getWinningPlayer() == null)
		{
			requestedItem.isFreeForAll(true);
			dropRegistrationMap.get(npcId).isInUse(false);
			return;
		}
			requestedItem.isDistributeItem(true);
			DropService.getInstance().requestDropItem(player, npcId, dropNpc.getCurrentIndex());		
	}

	/** 
	 * @param Displays messages when item gained via ROLLED
	 */	
	private void winningRollActions(Player player, int itemId, int npcId)
	{
		int nameId = DataManager.ITEM_DATA.getItemTemplate(itemId).getNameId();
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LOOT_GET_ITEM_ME(new DescriptionId(nameId)));
		
		if(player.isInGroup())
		{	
			for(Player member : dropRegistrationMap.get(npcId).getInRangePlayers())
			{
				if(!player.equals(member))
				PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_LOOT_GET_ITEM_OTHER(player.getName(),
						new DescriptionId(nameId)));
			}
		}
		if(player.isInAlliance())
		{
			for(PlayerAllianceMember allianceMember : player.getPlayerAlliance().getMembers())
			{
				Player member = allianceMember.getPlayer();
				if(!player.equals(member))
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_LOOT_GET_ITEM_OTHER(player.getName(),
							new DescriptionId(nameId)));
			}
		}
		return;
	}

	/**
	 * @param Displays messages/removes and shares kinah when item gained via BID
	 */	
	private void winningBidActions(Player player, int itemId, int npcId, long highestValue)
	{
		DropNpc dropNpc = dropRegistrationMap.get(npcId);
		
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PAY_ACCOUNT_ME(highestValue));
		ItemService.decreaseKinah(player, highestValue);
		
		if(player.isInGroup())
		{
			for(Player member : dropNpc.getInRangePlayers())
			{
				if(!player.equals(member))
				{
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_PAY_ACCOUNT_OTHER(player.getName(), highestValue));
					long distributeKinah = highestValue / (dropNpc.getGroupSize() - 1);
					ItemService.increaseKinah(member, distributeKinah);
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_PAY_DISTRIBUTE(highestValue, dropNpc.getGroupSize() - 1, distributeKinah));
				}
			}
		}
		if(player.isInAlliance())
		{
			for(PlayerAllianceMember allianceMember : player.getPlayerAlliance().getMembers())
			{
				Player member = allianceMember.getPlayer();
				if(!player.equals(member))
				{
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_PAY_ACCOUNT_OTHER(player.getName(), highestValue));
					long distributeKinah = highestValue / (dropNpc.getGroupSize() - 1);
					ItemService.increaseKinah(member, distributeKinah);
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_PAY_DISTRIBUTE(highestValue, dropNpc.getGroupSize() - 1, distributeKinah));
				}
			}
		}		
		return;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final DropService instance = new DropService();
	}
}