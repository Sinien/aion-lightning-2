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
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import javolution.util.FastMap;

import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Monster;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.group.GroupEvent;
import com.aionemu.gameserver.model.group.LootGroupRules;
import com.aionemu.gameserver.model.group.LootRuleType;
import com.aionemu.gameserver.model.group.PlayerGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_MEMBER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.restrictions.RestrictionsManager;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.utils.stats.StatFunctions;
import com.aionemu.gameserver.world.WorldType;

/**
 * @author Simple
 */
public class GroupService
{
	private static final Logger log = Logger.getLogger(GroupService.class);
	
	/**
	 * Caching group members
	 */
	private final FastMap<Integer, PlayerGroup>		groupMembers;

	/**
	 * Caching remove group member schedule
	 */
	private FastMap<Integer, ScheduledFuture<?>>	playerGroup;

	private List<Player>	inRangePlayers;

	public static final GroupService getInstance()
	{
		return SingletonHolder.instance;
	}

	
	/**
	 * @param playerGroup
	 */
	private GroupService()
	{
		groupMembers	= new FastMap<Integer, PlayerGroup>();
		playerGroup		= new FastMap<Integer, ScheduledFuture<?>>();
		
		log.info("GroupService: Initialized.");
	}


	/**
	 * This method will add a member to the group member cache
	 * 
	 * @param player
	 */
	private void addGroupMemberToCache(Player player)
	{
		if(!groupMembers.containsKey(player.getObjectId()))
			groupMembers.put(player.getObjectId(), player.getPlayerGroup());
	}

	private void removeGroupMemberFromCache(int playerObjId)
	{
		if(groupMembers.containsKey(playerObjId))
			groupMembers.remove(playerObjId);
	}

	/**
	 * @param playerObjId
	 * @return returns true if player is in the cache
	 */
	public boolean isGroupMember(int playerObjId)
	{
		return groupMembers.containsKey(playerObjId);
	}

	/**
	 * Returns the player's group
	 * 
	 * @param playerObjId
	 * @return PlayerGroup
	 */
	private PlayerGroup getGroup(int playerObjId)
	{
		return groupMembers.get(playerObjId);
	}

	/**
	 * This method will handle everything to a player that is invited for a group
	 * 
	 * @param inviter
	 * @param invited
	 */
	public void invitePlayerToGroup(final Player inviter, final Player invited)
	{
		if(RestrictionsManager.canInviteToGroup(inviter, invited))
		{
			RequestResponseHandler responseHandler = new RequestResponseHandler(inviter){
				@Override
				public void acceptRequest(Creature requester, Player responder)
				{
					final PlayerGroup group = inviter.getPlayerGroup();
					if(group != null && group.isFull())
						return;

					PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.REQUEST_GROUP_INVITE(invited.getName()));
					if(group != null)
					{
						inviter.getPlayerGroup().addPlayerToGroup(invited);
						addGroupMemberToCache(invited);
					}
					else
					{
						new PlayerGroup(IDFactory.getInstance().nextId(), inviter);
						inviter.getPlayerGroup().addPlayerToGroup(invited);
						addGroupMemberToCache(inviter);
						addGroupMemberToCache(invited);
					}
				}

				@Override
				public void denyRequest(Creature requester, Player responder)
				{
					PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.REJECT_GROUP_INVITE(responder.getName()));
				}
			};

			boolean result = invited.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_REQUEST_GROUP_INVITE,
				responseHandler);
			if(result)
			{
				PacketSendUtility.sendPacket(invited, new SM_QUESTION_WINDOW(
					SM_QUESTION_WINDOW.STR_REQUEST_GROUP_INVITE, 0, inviter.getName()));
			}
		}
	}

	/**
	 * @param player
	 */
	public void removePlayerFromGroup(Player player)
	{
		if(player.isInGroup())
		{
			final PlayerGroup group = player.getPlayerGroup();

			group.removePlayerFromGroup(player);
			removeGroupMemberFromCache(player.getObjectId());

			if(group.size() < 2)
				disbandGroup(group);
		}
	}

	/**
	 * @param player
	 */
	private void setGroupLeader(Player player)
	{
		final PlayerGroup group = player.getPlayerGroup();

		group.setGroupLeader(player);
		group.updateGroupUIToEvent(player.getPlayerGroup().getGroupLeader(), GroupEvent.CHANGELEADER);
	}

	/**
	 * @param status
	 * @param playerObjId
	 * @param player
	 */
	public void playerStatusInfo(int status, Player player)
	{
		switch(status)
		{
			case 2:
				removePlayerFromGroup(player);
				break;
			case 3:
				setGroupLeader(player);
				break;
			case 6:
				removePlayerFromGroup(player);
				break;
		}
	}

	/**
	 * @param player
	 * @param amount
	 */
	public void groupDistribution(Player player, long amount)
	{
		PlayerGroup pg = player.getPlayerGroup();
		if(pg == null)
			return;

		long availableKinah = player.getInventory().getKinahItem().getItemCount();
		if(availableKinah < amount)
		{
			// TODO retail message ?
			return;
		}

		long rewardcount = pg.size() - 1;
		if(rewardcount <= amount)
		{
			long reward = amount / rewardcount;

			for(Player groupMember : pg.getMembers())
			{
				if(groupMember.equals(player))
					ItemService.decreaseKinah(groupMember, amount);
				else
					ItemService.increaseKinah(groupMember, reward);
			}
		}
	}

	/**
	 * This method will send a reward if a player is in a group
	 * 
	 * @param player
	 */
	public void doReward(PlayerGroup group, Monster owner)
	{
		// Find Group Members and Determine Highest Level
		List<Player> players = new ArrayList<Player>();
		int partyLvlSum = 0;
		int highestLevel = 0;
		for(Player member : group.getMembers())
		{
			if (!member.isOnline()) continue;
			if(MathUtil.isIn3dRange(member, owner, GroupConfig.GROUP_MAX_DISTANCE))
			{
				if (member.getLifeStats().isAlreadyDead())
					continue;
				players.add(member);
				partyLvlSum += member.getLevel();
				if (member.getLevel() > highestLevel)
					highestLevel = member.getLevel();
			}
		}
		
		// All are dead or not nearby.
		if (players.size() == 0)
			return;
		
		//AP reward
		int apRewardPerMember = 0;
		WorldType worldType = owner.getWorldType();
		
		//WorldType worldType = sp.getWorld().getWorldMap(player.getWorldId()).getWorldType();
		if(worldType == WorldType.ABYSS)
		{
			// Split Evenly
			apRewardPerMember = Math.round(StatFunctions.calculateGroupAPReward(highestLevel, owner) / players.size());
		}
		
		// Exp reward
		long expReward = StatFunctions.calculateGroupExperienceReward(highestLevel, owner);
		
		// Exp Mod
		// TODO: Add logic to prevent power leveling. Players 10 levels below highest member should get 0 exp.
		double mod = 1;
		if (players.size() == 0)
			return;
		else if (players.size() > 1)
			mod = 1+(((players.size()-1)*10)/100);
		
		expReward *= mod; 

		for(Player member : players)
		{
			// Exp reward
			long currentExp = member.getCommonData().getExp();
			long reward = (expReward * member.getLevel())/partyLvlSum;
			reward *= member.getRates().getGroupXpRate();
			member.getCommonData().setExp(currentExp + reward);

			PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.EXP(Long.toString(reward)));

			// DP reward
			int currentDp = member.getCommonData().getDp();
			int dpReward = StatFunctions.calculateGroupDPReward(member, owner);
			member.getCommonData().setDp(dpReward + currentDp);
			
			// AP reward
			if (apRewardPerMember > 0)
				member.getCommonData().addAp(Math.round(apRewardPerMember * member.getRates().getApNpcRate()));
			
			QuestEngine.getInstance().onKill(new QuestEnv(owner, member, 0 , 0));
		}
		
		// Give Drop
		setInRangePlayers(players);
		Player leader = group.getGroupLeader();
		DropService.getInstance().registerDrop(owner, leader, highestLevel);
	}
	
	/**
	 * This method will send the show brand to every groupmember
	 * 
	 * @param playerGroup
	 * @param brandId
	 * @param targetObjectId
	 */
	public void showBrand(PlayerGroup playerGroup, int brandId, int targetObjectId)
	{
		for(Player member : playerGroup.getMembers())
		{
			PacketSendUtility.sendPacket(member, new SM_SHOW_BRAND(brandId, targetObjectId));
		}
	}

	/**
	 * This method is called when a group is disbanded
	 */
	private void disbandGroup(PlayerGroup group)
	{
		IDFactory.getInstance().releaseId(group.getGroupId());
		group.getGroupLeader().setPlayerGroup(null);
		PacketSendUtility.sendPacket(group.getGroupLeader(), SM_SYSTEM_MESSAGE.DISBAND_GROUP());
		group.disband();
	}

	/**
	 * @param player
	 */
	public void onLogin(Player activePlayer)
	{
		final PlayerGroup group = activePlayer.getPlayerGroup();

		// Send legion info packets
		PacketSendUtility.sendPacket(activePlayer, new SM_GROUP_INFO(group));
		for(Player member : group.getMembers())
		{
			if(!activePlayer.equals(member))
				PacketSendUtility.sendPacket(activePlayer, new SM_GROUP_MEMBER_INFO(group, member, GroupEvent.ENTER));
		}
	}

	/**
	 * @param playerGroupCache
	 *            the playerGroupCache to set
	 */
	private void addPlayerGroupCache(int playerObjId, ScheduledFuture<?> future)
	{
		if(!playerGroup.containsKey(playerObjId))
			playerGroup.put(playerObjId, future);
	}

	/**
	 * This method will remove a schedule to remove a player from a group
	 * 
	 * @param playerObjId
	 */
	private void cancelScheduleRemove(int playerObjId)
	{
		if(playerGroup.containsKey(playerObjId))
		{
			playerGroup.get(playerObjId).cancel(true);
			playerGroup.remove(playerObjId);
		}
	}

	/**
	 * This method will create a schedule to remove a player from a group
	 * 
	 * @param player
	 */
	public void scheduleRemove(final Player player)
	{
		ScheduledFuture<?> future = ThreadPoolManager.getInstance().schedule(new Runnable(){
			@Override
			public void run()
			{
				removePlayerFromGroup(player);
				playerGroup.remove(player.getObjectId());
			}
		}, GroupConfig.GROUP_REMOVE_TIME * 1000);
		addPlayerGroupCache(player.getObjectId(), future);
		player.getPlayerGroup().getMembers().remove(player.getObjectId());
		
		for(Player groupMember : player.getPlayerGroup().getMembers())
		{
			// TODO: MISSING SEND PARTY MEMBER PACKETS
			PacketSendUtility.sendPacket(groupMember, SM_SYSTEM_MESSAGE.PARTY_HE_BECOME_OFFLINE(player.getName()));
		}
	}

	/**
	 * @param player
	 */
	public void setGroup(Player player)
	{
		if(!isGroupMember(player.getObjectId()))
			return;

		final PlayerGroup group = getGroup(player.getObjectId());
		if(group.size() < 2)
		{
			removeGroupMemberFromCache(player.getObjectId());
			cancelScheduleRemove(player.getObjectId());
			return;
		}
		player.setPlayerGroup(group);
		group.onGroupMemberLogIn(player);
		cancelScheduleRemove(player.getObjectId());
		if(group.getGroupLeader().getObjectId() == player.getObjectId())
			group.setGroupLeader(player);
	}

	/**
	 * @return FastMap<Integer, Boolean>
	 */
	public List<Integer> getMembersToRegistrateByRules(Player player, PlayerGroup group, Npc npc)
	{
		LootGroupRules lootRules = group.getLootGroupRules();
		LootRuleType lootRule = lootRules.getLootRule();
		List<Integer> luckyMembers = new ArrayList<Integer>();

		switch(lootRule)
		{
			case ROUNDROBIN:
				int roundRobinMember = group.getRoundRobinMember(npc);
				if(roundRobinMember != 0)
				{
					luckyMembers.add(roundRobinMember);
					break;
				} // if no member is found then the loot is FREEFORALL.
			case FREEFORALL:
				luckyMembers = getGroupMembers(group, false);
				break;
			case LEADER:
				luckyMembers.add(group.getGroupLeader().getObjectId());
				break;
		}
		return luckyMembers;
	}

	/**
	 * This method will get all group members
	 * 
	 * @param group
	 * @param except
	 * @return list of group members
	 */
	public List<Integer> getGroupMembers(final PlayerGroup group, boolean except)
	{
		List<Integer> luckyMembers = new ArrayList<Integer>();
		for(int memberObjId : group.getMemberObjIds())
		{
			if(except)
			{
				if(group.getGroupLeader().getObjectId() != memberObjId)
					luckyMembers.add(memberObjId);
			}
			else
				luckyMembers.add(memberObjId);
		}
		return luckyMembers;
	}

	/**
	 * @param player
	 */
	public Player getLuckyPlayer(Player player)
	{
		final PlayerGroup group = player.getPlayerGroup();
		switch(group.getLootGroupRules().getAutodistribution())
		{
			case NORMAL:
				return player;
			case ROLL_DICE:
				// NOT FINISHED YET
				return player;
			case BID:
				// NOT FINISHED YET
				return player;
		}
		return player;
	}
	
	/**
	 * @param inRangePlayers the inRangePlayers to set
	 */
	public void setInRangePlayers(List<Player> inRangePlayers)
	{
		this.inRangePlayers = inRangePlayers;
	}

	/**
	 * @return the inRangePlayers
	 */
	public List<Player> getInRangePlayers()
	{
		return inRangePlayers;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final GroupService instance = new GroupService();
	}

}
