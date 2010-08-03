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

import javolution.util.FastMap;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.controllers.attack.KillList;
import com.aionemu.gameserver.model.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.alliance.PlayerAllianceMember;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.group.PlayerGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;
import com.aionemu.gameserver.utils.stats.StatFunctions;

/**
 * @author Sarynth
 *
 */
public class PvpService
{
	public static final PvpService getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private FastMap<Integer, KillList> pvpKillLists;
	
	private PvpService()
	{
		pvpKillLists = new FastMap<Integer, KillList>();
	}
	
	/**
	 * @param winnerId
	 * @param victimId
	 * @return
	 */
	private int getKillsFor(int winnerId, int victimId)
	{
		KillList winnerKillList = pvpKillLists.get(winnerId);
		
		if (winnerKillList == null)
			return 0;
		return winnerKillList.getKillsFor(victimId);
	}

	/**
	 * @param winnerId
	 * @param victimId
	 */
	private void addKillFor(int winnerId, int victimId)
	{
		KillList winnerKillList = pvpKillLists.get(winnerId);
		if (winnerKillList == null)
		{
			winnerKillList = new KillList();
			pvpKillLists.put(winnerId, winnerKillList);
		}
		winnerKillList.addKillFor(victimId);
	}
	
	/**
	 * @param victim
	 */
	public void doReward(Player victim)
	{
		// winner is the player that receives the kill count
		final Player winner = victim.getAggroList().getMostPlayerDamage();
		
		int totalDamage = victim.getAggroList().getTotalDamage();
		
		if (totalDamage == 0 || winner == null)
		{
			return;
		}
		
		// Add Player Kill to record.
		if (this.getKillsFor(winner.getObjectId(), victim.getObjectId()) < CustomConfig.MAX_DAILY_PVP_KILLS)
			winner.getAbyssRank().setAllKill();
		
		// Announce that player has died.
		PacketSendUtility.broadcastPacketAndReceive(victim,
			SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_FRIENDLY_DEATH_TO_B(victim.getName(), winner.getName()));
		
		// Keep track of how much damage was dealt by players
		// so we can remove AP based on player damage...
		int playerDamage = 0;
		boolean success = false;
		
		// Distribute AP to groups and players that had damage.
		for(AggroInfo aggro : victim.getAggroList().getFinalDamageList(true))
		{
			if (aggro.getAttacker() instanceof Player)
			{
				success = rewardPlayer(victim, totalDamage, aggro);
			}
			else if (aggro.getAttacker() instanceof PlayerGroup)
			{
				success = rewardPlayerGroup(victim, totalDamage, aggro);
			}
			else if (aggro.getAttacker() instanceof PlayerAlliance)
			{
				success = rewardPlayerAlliance(victim, totalDamage, aggro);
			}
			
			// Add damage last, so we don't include damage from same race. (Duels, Arena)
			if (success)
				playerDamage += aggro.getDamage();
		}
		
		// Apply lost AP to defeated player
		final int apLost = StatFunctions.calculatePvPApLost(victim, winner);
		final int apActuallyLost = (int)(apLost * playerDamage / totalDamage);
		
		if (apActuallyLost > 0)
			victim.getCommonData().addAp(-apActuallyLost);
			
	}

	
	/**
	 * @param victim
	 * @param totalDamage
	 * @param aggro
	 * @return true if group is not same race
	 */
	private boolean rewardPlayerGroup(Player victim, int totalDamage, AggroInfo aggro)
	{
		// Reward Group
		PlayerGroup group = ((PlayerGroup)aggro.getAttacker());
		
		// Don't Reward Player of Same Faction.
		// TODO: NPE if leader is offline? Store race in group.
		if (group.getGroupLeader().getCommonData().getRace() == victim.getCommonData().getRace())
			return false;
		
		// Find group members in range
		List<Player> players = new ArrayList<Player>();
		
		// Find highest rank and level in local group
		int maxRank = AbyssRankEnum.GRADE9_SOLDIER.getId();
		int maxLevel = 0;
		
		for(Player member : group.getMembers())
		{
			if(MathUtil.isIn3dRange(member, victim, GroupConfig.GROUP_MAX_DISTANCE))
			{
				// Don't distribute AP to a dead player!
				if (!member.getLifeStats().isAlreadyDead())
				{
					players.add(member);
					if (member.getLevel() > maxLevel)
						maxLevel = member.getLevel();
					if (member.getAbyssRank().getRank().getId() > maxRank)
						maxRank = member.getAbyssRank().getRank().getId();
				}
			}
		}
		
		// They are all dead or out of range.
		if (players.size() == 0)
			return false;
		
		int baseApReward = StatFunctions.calculatePvpApGained(victim, maxRank, maxLevel);
		float groupApPercentage = (float)aggro.getDamage() / totalDamage;
		int apRewardPerMember = Math.round(baseApReward * groupApPercentage / players.size());
		
		if (apRewardPerMember > 0)
		{
			for(Player member : players)
			{
				int memberApGain = 1;
				if (this.getKillsFor(member.getObjectId(), victim.getObjectId()) < CustomConfig.MAX_DAILY_PVP_KILLS)
					memberApGain = Math.round(apRewardPerMember * member.getRates().getApPlayerRate());
				member.getCommonData().addAp(memberApGain);
				this.addKillFor(member.getObjectId(), victim.getObjectId());
			}
		}
		
		return true;
	}

	
	/**
	 * @param victim
	 * @param totalDamage
	 * @param aggro
	 * @return true if group is not same race
	 */
	private boolean rewardPlayerAlliance(Player victim, int totalDamage, AggroInfo aggro)
	{
		// Reward Alliance
		PlayerAlliance alliance = ((PlayerAlliance)aggro.getAttacker());
		
		// Don't Reward Player of Same Faction.
		if (alliance.getCaptain().getCommonData().getRace() == victim.getCommonData().getRace())
			return false;
		
		// Find group members in range
		List<Player> players = new ArrayList<Player>();
		
		// Find highest rank and level in local group
		int maxRank = AbyssRankEnum.GRADE9_SOLDIER.getId();
		int maxLevel = 0;
		
		for(PlayerAllianceMember allianceMember : alliance.getMembers())
		{
			if (!allianceMember.isOnline()) continue;
			Player member = allianceMember.getPlayer();
			if(MathUtil.isIn3dRange(member, victim, GroupConfig.GROUP_MAX_DISTANCE))
			{
				// Don't distribute AP to a dead player!
				if (!member.getLifeStats().isAlreadyDead())
				{
					players.add(member);
					if (member.getLevel() > maxLevel)
						maxLevel = member.getLevel();
					if (member.getAbyssRank().getRank().getId() > maxRank)
						maxRank = member.getAbyssRank().getRank().getId();
				}
			}
		}
		
		// They are all dead or out of range.
		if (players.size() == 0)
			return false;
		
		int baseApReward = StatFunctions.calculatePvpApGained(victim, maxRank, maxLevel);
		float groupApPercentage = (float)aggro.getDamage() / totalDamage;
		int apRewardPerMember = Math.round(baseApReward * groupApPercentage / players.size());
		
		if (apRewardPerMember > 0)
		{
			for(Player member : players)
			{
				int memberApGain = 1;
				if (this.getKillsFor(member.getObjectId(), victim.getObjectId()) < CustomConfig.MAX_DAILY_PVP_KILLS)
					memberApGain = Math.round(apRewardPerMember * member.getRates().getApPlayerRate());
				member.getCommonData().addAp(memberApGain);
				this.addKillFor(member.getObjectId(), victim.getObjectId());
			}
		}
		
		return true;
	}


	/**
	 * @param victim
	 * @param totalDamage
	 * @param aggro
	 * @return true if player is not same race
	 */
	private boolean rewardPlayer(Player victim, int totalDamage, AggroInfo aggro)
	{
		// Reward Player
		Player winner = ((Player)aggro.getAttacker());
		
		// Don't Reward Player of Same Faction.
		if (winner.getCommonData().getRace() == victim.getCommonData().getRace())
			return false;
		
		int baseApReward = 1;
		
		if (this.getKillsFor(winner.getObjectId(), victim.getObjectId()) < CustomConfig.MAX_DAILY_PVP_KILLS)
			baseApReward = StatFunctions.calculatePvpApGained(victim, winner.getAbyssRank().getRank().getId(), winner.getLevel());
		
		int apPlayerReward = Math.round(baseApReward  * winner.getRates().getApPlayerRate() * aggro.getDamage() / totalDamage);

		winner.getCommonData().addAp(apPlayerReward);
		this.addKillFor(winner.getObjectId(), victim.getObjectId());
		
		return true;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final PvpService instance = new PvpService();
	}
}
