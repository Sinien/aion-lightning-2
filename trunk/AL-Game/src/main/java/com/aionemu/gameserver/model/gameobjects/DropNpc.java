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
package com.aionemu.gameserver.model.gameobjects;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Simple
 */
public class DropNpc
{
	private List<Integer>	allowedList			= new ArrayList<Integer>();
	private List<Player>	inRangePlayers		= new ArrayList<Player>();
	private List<Player>	playerStatus		= new ArrayList<Player>();	
	private Player			lootingPlayer		= null;
	private int				distributionType	= 0;
	private boolean			inUse				= false;
	private int				currentIndex		= 0;
	private int				groupSize			= 0;

	public DropNpc(List<Integer> allowedList)
	{
		this.allowedList = allowedList;
	}

	/**
	 * Everyone is allowed to loot
	 */
	public void setFreeLooting()
	{
		allowedList = null;
	}

	/**
	 * @return true if playerObjId is found in list
	 */
	public boolean containsKey(int playerObjId)
	{
		if(allowedList == null)
			return true;
		return allowedList.contains(playerObjId);
	}

	/**
	 * @param player
	 *            the lootingPlayer to set
	 */
	public void setBeingLooted(Player player)
	{
		this.lootingPlayer = player;
	}

	/**
	 * @return lootingPlayer
	 */
	public Player getBeingLooted()
	{
		return lootingPlayer;
	}	
	
	/**
	 * @return the beingLooted
	 */
	public boolean isBeingLooted()
	{
		return lootingPlayer != null;
	}

	/**
	 * @param distributionType
	 */
	public void setDistributionType(int distributionType)
	{
		this.distributionType = distributionType;
	}

	/**
	 * @return the DistributionType
	 */
	public int getDistributionType()
	{
		return distributionType;
	}

	/**
	 * @param inUse
	 */
	public void isInUse(boolean inUse)
	{
		this.inUse = inUse;
	}

	/**
	 * @return inUse
	 */
	public boolean isInUse()
	{
		return inUse;
	}

	/**
	 * @param currentIndex
	 */
	public void setCurrentIndex(int currentIndex)
	{
		this.currentIndex = currentIndex;
	}

	/**
	 * @return currentIndex
	 */
	public int getCurrentIndex()
	{
		return currentIndex;
	}

	/**
	 * @param groupSize
	 */
	public void setGroupSize(int groupSize)
	{
		this.groupSize = groupSize;
	}

	/**
	 * @return groupSize
	 */
	public int getGroupSize()
	{
		return groupSize;
	}

	/**
	 * @param inRangePlayers
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

	/**
	 * @param addPlayerStatus
	 */
	public void addPlayerStatus(Player player)
	{
		playerStatus.add(player);
	}

	/**
	 * @param delPlayerStatus
	 */
	public void delPlayerStatus(Player player)
	{
		playerStatus.remove(player);
	}
	
	/**
	 * @return the playerStatus
	 */
	public List<Player> getPlayerStatus()
	{
		return playerStatus;
	}
	
	/**
	 * @return true if player is found in list
	 */
	public boolean containsPlayerStatus(Player player)
	{
		return playerStatus.contains(player);
	}
}
