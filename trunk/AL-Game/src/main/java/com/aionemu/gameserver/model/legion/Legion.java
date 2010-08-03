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
package com.aionemu.gameserver.model.legion;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import com.aionemu.gameserver.configs.main.LegionConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.World;

/**
 * @author Simple
 * 
 */
public class Legion
{
	/** Static Permission settings **/
	private static final int						PERMISSION1_MIN				= 0x60;
	private static final int						PERMISSION2_MIN				= 0x00;
	private static final int						LEGIONAR_PERMISSION2_MAX	= 0x08;
	private static final int						CENTURION_PERMISSION1_MAX	= 0x7C;
	private static final int						CENTURION_PERMISSION2_MAX	= 0x0E;

	/** Legion Information **/
	private int										legionId					= 0;
	private String									legionName					= "";
	private int										legionLevel					= 1;
	private int										legionRank					= 0;
	private int										contributionPoints			= 0;
	private List<Integer>							legionMembers				= new ArrayList<Integer>();
	private static final int						legionarPermission1			= 0x40;
	private int										legionarPermission2			= 0x00;
	private int										centurionPermission1		= 0x60;
	private int										centurionPermission2		= 0x00;
	private int										disbandTime;
	private TreeMap<Timestamp, String>				announcementList			= new TreeMap<Timestamp, String>();
	private LegionEmblem							legionEmblem				= new LegionEmblem();
	private LegionWarehouse							legionWarehouse;
	private SortedSet<LegionHistory>				legionHistory;

	/**
	 * Only called when a legion is created!
	 * 
	 * @param legionId
	 * @param legionName
	 */
	public Legion(int legionId, String legionName)
	{
		this();
		this.legionId = legionId;
		this.legionName = legionName;
	}

	/**
	 * Only called when a legion is loaded!
	 * 
	 * @param legionId
	 * @param legionName
	 */
	public Legion()
	{
		this.legionWarehouse = new LegionWarehouse(this);
		this.legionHistory = new TreeSet<LegionHistory>(new Comparator<LegionHistory>(){

			@Override
			public int compare(LegionHistory o1, LegionHistory o2)
			{
				return o1.getTime().getTime() < o2.getTime().getTime() ? 1 : -1;
			}
			
		});
	}

	/**
	 * @param legionId
	 *            the legionId to set
	 */
	public void setLegionId(int legionId)
	{
		this.legionId = legionId;
	}

	/**
	 * @return the legionId
	 */
	public int getLegionId()
	{
		return legionId;
	}

	/**
	 * @param legionName
	 *            the legionName to set
	 */
	public void setLegionName(String legionName)
	{
		this.legionName = legionName;
	}

	/**
	 * @return the legionName
	 */
	public String getLegionName()
	{
		return legionName;
	}

	/**
	 * @param legionMembers
	 *            the legionMembers to set
	 */
	public void setLegionMembers(ArrayList<Integer> legionMembers)
	{
		this.legionMembers = legionMembers;
	}

	/**
	 * @return the legionMembers
	 */
	public List<Integer> getLegionMembers()
	{
		return legionMembers;
	}

	/**
	 * @return the online legionMembers
	 */
	public ArrayList<Player> getOnlineLegionMembers()
	{
		ArrayList<Player> onlineLegionMembers = new ArrayList<Player>();
		for(int legionMemberObjId : legionMembers)
		{
			Player onlineLegionMember = World.getInstance().findPlayer(legionMemberObjId);
			if(onlineLegionMember != null)
				onlineLegionMembers.add(onlineLegionMember);
		}
		return onlineLegionMembers;
	}

	/**
	 * Add a legionMember to the legionMembers list
	 * 
	 * @param legionMember
	 */
	public boolean addLegionMember(int playerObjId)
	{
		if(canAddMember())
		{
			legionMembers.add(playerObjId);
			return true;
		}
		return false;
	}

	/**
	 * Delete a legionMember from the legionMembers list
	 * 
	 * @param playerObjId
	 */
	public void deleteLegionMember(int playerObjId)
	{
		legionMembers.remove(new Integer(playerObjId));
	}

	/**
	 * This method will set the permissions
	 * 
	 * @param legionarPermission2
	 * @param centurionPermission1
	 * @param centurionPermission2
	 * @return true or false
	 */
	public boolean setLegionPermissions(int legionarPermission2, int centurionPermission1, int centurionPermission2)
	{
		if(checkPermissions(legionarPermission2, centurionPermission1, centurionPermission2))
		{
			this.legionarPermission2 = legionarPermission2;
			this.centurionPermission1 = centurionPermission1;
			this.centurionPermission2 = centurionPermission2;
			return true;
		}
		return false;
	}

	/**
	 * Check if all permissions are correct
	 * 
	 * @return true or false
	 */
	private boolean checkPermissions(int legionarPermission2, int centurionPermission1, int centurionPermission2)
	{
		if(legionarPermission2 < PERMISSION2_MIN || legionarPermission2 > LEGIONAR_PERMISSION2_MAX)
			return false;
		if(centurionPermission1 < PERMISSION1_MIN || centurionPermission1 > CENTURION_PERMISSION1_MAX)
			return false;
		if(centurionPermission2 < PERMISSION2_MIN || centurionPermission2 > CENTURION_PERMISSION2_MAX)
			return false;
		return true;
	}

	/**
	 * @return the legionarPermission1
	 */
	public int getLegionarPermission1()
	{
		return legionarPermission1;
	}

	/**
	 * @return the legionarPermission2
	 */
	public int getLegionarPermission2()
	{
		return legionarPermission2;
	}

	/**
	 * @return the centurionPermission1
	 */
	public int getCenturionPermission1()
	{
		return centurionPermission1;
	}

	/**
	 * @return the centurionPermission2
	 */
	public int getCenturionPermission2()
	{
		return centurionPermission2;
	}

	/**
	 * @return the legionLevel
	 */
	public int getLegionLevel()
	{
		return legionLevel;
	}

	/**
	 * @param legionLevel
	 */
	public void setLegionLevel(int legionLevel)
	{
		this.legionLevel = legionLevel;
	}

	/**
	 * @param legionRank
	 *            the legionRank to set
	 */
	public void setLegionRank(int legionRank)
	{
		this.legionRank = legionRank;
	}

	/**
	 * @return the legionRank
	 */
	public int getLegionRank()
	{
		return legionRank;
	}

	/**
	 * @param contributionPoints
	 *            the contributionPoints to set
	 */
	public void addContributionPoints(int contributionPoints)
	{
		this.contributionPoints = this.contributionPoints + contributionPoints;
	}

	/**
	 * @param newPoints
	 */
	public void setContributionPoints(int contributionPoints)
	{
		this.contributionPoints = contributionPoints;
	}

	/**
	 * @return the contributionPoints
	 */
	public int getContributionPoints()
	{
		return contributionPoints;
	}

	/**
	 * This method will check whether a legion has enough members to level up
	 * 
	 * @return true or false
	 */
	public boolean hasRequiredMembers()
	{
		switch(getLegionLevel())
		{
			case 1:
				if(getLegionMembers().size() >= LegionConfig.LEGION_LEVEL2_REQUIRED_MEMBERS)
					return true;
				break;
			case 2:
				if(getLegionMembers().size() >= LegionConfig.LEGION_LEVEL3_REQUIRED_MEMBERS)
					return true;
				break;
			case 3:
				if(getLegionMembers().size() >= LegionConfig.LEGION_LEVEL4_REQUIRED_MEMBERS)
					return true;
				break;
			case 4:
				if(getLegionMembers().size() >= LegionConfig.LEGION_LEVEL5_REQUIRED_MEMBERS)
					return true;
				break;
		}
		return false;
	}

	/**
	 * This method will return the kinah price required to level up
	 * 
	 * @return int
	 */
	public int getKinahPrice()
	{
		switch(getLegionLevel())
		{
			case 1:
				return LegionConfig.LEGION_LEVEL2_REQUIRED_KINAH;
			case 2:
				return LegionConfig.LEGION_LEVEL3_REQUIRED_KINAH;
			case 3:
				return LegionConfig.LEGION_LEVEL4_REQUIRED_KINAH;
			case 4:
				return LegionConfig.LEGION_LEVEL5_REQUIRED_KINAH;
		}
		return 0;
	}

	/**
	 * This method will return the contribution points required to level up
	 * 
	 * @return int
	 */
	public int getContributionPrice()
	{
		switch(getLegionLevel())
		{
			case 1:
				return LegionConfig.LEGION_LEVEL2_REQUIRED_CONTRIBUTION;
			case 2:
				return LegionConfig.LEGION_LEVEL3_REQUIRED_CONTRIBUTION;
			case 3:
				return LegionConfig.LEGION_LEVEL4_REQUIRED_CONTRIBUTION;
			case 4:
				return LegionConfig.LEGION_LEVEL5_REQUIRED_CONTRIBUTION;
		}
		return 0;
	}

	/**
	 * This method will return true if a legion is able to add a member
	 * 
	 * @return
	 */
	private boolean canAddMember()
	{
		switch(getLegionLevel())
		{
			case 1:
				if(getLegionMembers().size() < LegionConfig.LEGION_LEVEL1_MAX_MEMBERS)
					return true;
				break;
			case 2:
				if(getLegionMembers().size() < LegionConfig.LEGION_LEVEL2_MAX_MEMBERS)
					return true;
				break;
			case 3:
				if(getLegionMembers().size() < LegionConfig.LEGION_LEVEL3_MAX_MEMBERS)
					return true;
				break;
			case 4:
				if(getLegionMembers().size() < LegionConfig.LEGION_LEVEL4_MAX_MEMBERS)
					return true;
				break;
			case 5:
				if(getLegionMembers().size() < LegionConfig.LEGION_LEVEL5_MAX_MEMBERS)
					return true;
				break;
		}
		return false;
	}

	/**
	 * @param announcementList
	 *            the announcementList to set
	 */
	public void setAnnouncementList(TreeMap<Timestamp, String> announcementList)
	{
		this.announcementList = announcementList;
	}

	/**
	 * This method will add a new announcement to the list
	 */
	public void addAnnouncementToList(Timestamp unixTime, String announcement)
	{
		this.announcementList.put(unixTime, announcement);
	}

	/**
	 * This method removes the first entry
	 */
	public void removeFirstEntry()
	{
		this.announcementList.remove(this.announcementList.firstEntry().getKey());
	}

	/**
	 * @return the announcementList
	 */
	public TreeMap<Timestamp, String> getAnnouncementList()
	{
		return this.announcementList;
	}

	/**
	 * @return the currentAnnouncement
	 */
	public Entry<Timestamp, String> getCurrentAnnouncement()
	{
		if(this.announcementList.size() > 0)
			return this.announcementList.lastEntry();
		return null;
	}

	/**
	 * @param disbandTime
	 *            the disbandTime to set
	 */
	public void setDisbandTime(int disbandTime)
	{
		this.disbandTime = disbandTime;
	}

	/**
	 * @return the disbandTime
	 */
	public int getDisbandTime()
	{
		return disbandTime;
	}

	/**
	 * @return true if currently disbanding
	 */
	public boolean isDisbanding()
	{
		if(disbandTime > 0)
		{
			return true;
		}
		return false;
	}

	/**
	 * This function checks if object id is in list
	 * 
	 * @param memberObjId
	 * @return true if ID is found in the list
	 */
	public boolean isMember(int playerObjId)
	{
		return legionMembers.contains(playerObjId);
	}

	/**
	 * @param legionEmblem
	 *            the legionEmblem to set
	 */
	public void setLegionEmblem(LegionEmblem legionEmblem)
	{
		this.legionEmblem = legionEmblem;
	}

	/**
	 * @return the legionEmblem
	 */
	public LegionEmblem getLegionEmblem()
	{
		return legionEmblem;
	}

	/**
	 * @param legionWarehouse
	 *            the legionWarehouse to set
	 */
	public void setLegionWarehouse(LegionWarehouse legionWarehouse)
	{
		this.legionWarehouse = legionWarehouse;
	}

	/**
	 * @return the legionWarehouse
	 */
	public LegionWarehouse getLegionWarehouse()
	{
		return legionWarehouse;
	}

	/**
	 * Get warehouse slots
	 * 
	 * @return warehouse slots
	 */
	public int getWarehouseSlots()
	{
		switch(getLegionLevel())
		{
			case 1:
				return 24;
			case 2:
				return 32;
			case 3:
				return 40;
			case 4:
				return 48;
			case 5:
				return 56;
		}
		return 24;
	}

	/**
	 * @return the legionHistory
	 */
	public Collection<LegionHistory> getLegionHistory()
	{
		return this.legionHistory;
	}

	/**
	 * @param history
	 */
	public void addHistory(LegionHistory history)
	{
		this.legionHistory.add(history);
	}
}
