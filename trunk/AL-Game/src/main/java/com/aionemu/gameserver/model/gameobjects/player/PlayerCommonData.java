/*
 * This file is part of aion-emu <aion-emu.com>.
 *
 *  aion-emu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-emu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-emu.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.model.gameobjects.player;

import java.sql.Timestamp;

import org.apache.log4j.Logger;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.stats.StatEnum;
import com.aionemu.gameserver.model.templates.VisibleObjectTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANK_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DP_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_EDIT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATS_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATUPDATE_DP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATUPDATE_EXP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.stats.XPLossEnum;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * This class is holding base information about player, that may be used even when player itself is not online.
 * 
 * @author Luno
 * 
 */
public class PlayerCommonData extends VisibleObjectTemplate
{
	private static final Logger			log	= Logger.getLogger(PlayerCommonData.class);

	private final int		playerObjId;
	private Race			race;
	private String			name;
	private PlayerClass		playerClass;
	/** Should be changed right after character creation **/
	private int				level = 0;
	private long			exp = 0;
	private long			expRecoverable = 0;
	private Gender			gender;
	private Timestamp		lastOnline;
	private boolean 		online;
	private String 			note;
	private WorldPosition	position;
	private int 			cubeSize = 0;
	private int 			warehouseSize = 0;
	private int				advencedStigmaSlotSize = 0;
	private int			    bindPoint;
	private int             titleId = -1;
	private int				dp = 0;
	private int 			mailboxLetters;

	//TODO: Move all function to playerService or Player class.
	public PlayerCommonData(int objId)
	{
		this.playerObjId = objId;
	}

	public int getPlayerObjId()
	{
		return playerObjId;
	}

	public long getExp()
	{
		return this.exp;
	}
	public int getCubeSize()
	{
		return this.cubeSize;
	}
	public void setCubesize(int cubeSize)
	{
		this.cubeSize = cubeSize;
	}
	/**
	 * @return the advencedStigmaSlotSize
	 */
	public int getAdvencedStigmaSlotSize()
	{
		return advencedStigmaSlotSize;
	}

	/**
	 * @param advencedStigmaSlotSize the advencedStigmaSlotSize to set
	 */
	public void setAdvencedStigmaSlotSize(int advencedStigmaSlotSize)
	{
		this.advencedStigmaSlotSize = advencedStigmaSlotSize;
	}

	public long getExpShown()
	{
		return this.exp - DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(this.level);
	}

	public long getExpNeed()
	{
		if (this.level == DataManager.PLAYER_EXPERIENCE_TABLE.getMaxLevel())
		{
			return 0;
		}			
		return DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(this.level + 1) - DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(this.level);
	}

	/**
	 * calculate the lost experience
	 * must be called before setexp
	 * @author Jangan
	 */
	public void calculateExpLoss()
	{
		long expLost = XPLossEnum.getExpLoss(this.level, this.getExpNeed()); // This Calculates all the exp lost when dieing.
		int unrecoverable = (int) (expLost * 0.33333333); // This is 1000% Correct
		int recoverable = (int) expLost - unrecoverable;// This is 1000% Correct
		long allExpLost = recoverable + this.expRecoverable; // lol some crack headed formula ???

		// This loops states that if the unrecoverable exp is bigger than your current exp
		// we delete all your exp and go back to 0 pretty much.
		if(this.getExpShown() > unrecoverable)
		{
			this.exp = this.exp - unrecoverable;
		}
		else
		{
			this.exp = this.exp - this.getExpShown();
		}
		if(this.getExpShown() > recoverable)
		{
			this.expRecoverable = allExpLost;
			this.exp = this.exp - recoverable;
		}
		else
		{
			this.expRecoverable = this.expRecoverable + this.getExpShown();
			this.exp = this.exp - this.getExpShown();
		}
		if (this.getPlayer() != null)
			PacketSendUtility.sendPacket(this.getPlayer(),
				new SM_STATUPDATE_EXP(this.getExpShown(), this.getExpRecoverable(), this.getExpNeed()));
	}

	public void setRecoverableExp(long expRecoverable)
	{
		this.expRecoverable = expRecoverable;
	}

	public void resetRecoverableExp()
	{
		long el = this.expRecoverable;
		this.expRecoverable = 0;
		this.setExp(this.exp + el);
	}

	public long getExpRecoverable()
	{
		return this.expRecoverable;
	}
	
	/**
	 * 
	 * @param value
	 */
	public void addExp(long value)
	{
		this.setExp(this.exp + value);
		if(this.getPlayer() != null)
		{
			PacketSendUtility.sendPacket(this.getPlayer(),SM_SYSTEM_MESSAGE.EXP(Long.toString(value)));
		}
	}

	/**
	 * sets the exp value
	 * @param exp
	 */
	public void setExp(long exp)
	{
		//maxLevel is 51 but in game 50 should be shown with full XP bar
		int maxLevel = DataManager.PLAYER_EXPERIENCE_TABLE.getMaxLevel();

		if (getPlayerClass() != null && getPlayerClass().isStartingClass())
			maxLevel = 10;

		long maxExp = DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(maxLevel);
		int level = 1;

		if (exp > maxExp)
		{
			exp = maxExp;
		}

		//make sure level is never larger than maxLevel-1
		while ((level + 1) != maxLevel && exp >= DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(level + 1))
		{
			level++;
		}

		if (level != this.level)
		{
			this.level = level;
			this.exp = exp;

			if(this.getPlayer() != null)
			{
				upgradePlayer();
			}	
		}
		else
		{
			this.exp = exp;

			if(this.getPlayer() != null)
			{
				PacketSendUtility.sendPacket(this.getPlayer(),
					new SM_STATUPDATE_EXP(this.getExpShown(), this.getExpRecoverable(), this.getExpNeed()));
			}
		}
	}

	/**
	 * Do necessary player upgrades on level up
	 */
	public void upgradePlayer()
	{
		Player player = this.getPlayer();
		if(player != null)
		{
			player.getController().upgradePlayer(level);
		}
	}

	public void addAp(int value)
	{
		Player player = this.getPlayer();
		
		if (player == null)
			return;
		
		// Notify player of AP gained (This should happen before setAp happens.)
		// TODO: Find System Message for "You have lost %d Abyss Points." (Lost instead of Gained)
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.EARNED_ABYSS_POINT(String.valueOf(value)));
		
		// Set the new AP value
		this.setAp(value);
		
		// Add Abyss Points to Legion
		if(player.isLegionMember() && value > 0)
		{
			player.getLegion().addContributionPoints(value);
			PacketSendUtility.broadcastPacketToLegion(player.getLegion(), new SM_LEGION_EDIT(0x03, player.getLegion()));
		}
	}
	
	public void setAp(int value)
	{
		Player player = this.getPlayer();
		
		if (player == null)
			return;
		
		AbyssRank rank = player.getAbyssRank();
		
		int oldAbyssRank = rank.getRank().getId();
		
		rank.addAp(value);
		
		if (rank.getRank().getId() != oldAbyssRank)
		{
			PacketSendUtility.broadcastPacket(player, new SM_ABYSS_RANK_UPDATE(player));
			
			// Apparently we are not in our own known list... so we must tell ourselves as well
			PacketSendUtility.sendPacket(player, new SM_ABYSS_RANK_UPDATE(player));
		}
		
		//if abyss rank increase give abyss skills
		if (rank.getRank().getId() > oldAbyssRank)
		{
			if (this.getRace().getRaceId()==0) //ELYOS
			{	
					switch( rank.getRank().getId() )
					{
						case 14:	//
							player.getSkillList().addSkill(player, 9737, 1, true);
							player.getSkillList().addSkill(player, 9747, 1, true);
							break;
						case 15:
							player.getSkillList().removeSkill(player, 9737);
							player.getSkillList().removeSkill(player, 9747);
							player.getSkillList().addSkill(player, 9738, 1, true);
							player.getSkillList().addSkill(player, 9748, 1, true);
							player.getSkillList().addSkill(player, 9751, 1, true);
							break;
						case 16:
							player.getSkillList().removeSkill(player, 9738);
							player.getSkillList().removeSkill(player, 9748);
							player.getSkillList().removeSkill(player, 9751);
							player.getSkillList().addSkill(player, 9739, 1, true);
							player.getSkillList().addSkill(player, 9749, 1, true);
							player.getSkillList().addSkill(player, 9751, 1, true);
							player.getSkillList().addSkill(player, 9755, 1, true);
							break;
						case 17:
							player.getSkillList().removeSkill(player, 9739);
							player.getSkillList().removeSkill(player, 9749);
							player.getSkillList().removeSkill(player, 9751);
							player.getSkillList().removeSkill(player, 9755);
							player.getSkillList().addSkill(player, 9740, 1, true);
							player.getSkillList().addSkill(player, 9750, 1, true);
							player.getSkillList().addSkill(player, 9752, 1, true);
							player.getSkillList().addSkill(player, 9755, 1, true);
							player.getSkillList().addSkill(player, 9756, 1, true);
							break;
						case 18:
							player.getSkillList().removeSkill(player, 9740);
							player.getSkillList().removeSkill(player, 9750);
							player.getSkillList().removeSkill(player, 9752);
							player.getSkillList().removeSkill(player, 9755);
							player.getSkillList().removeSkill(player, 9756);
							player.getSkillList().addSkill(player, 9741, 1, true);
							player.getSkillList().addSkill(player, 9750, 1, true);
							player.getSkillList().addSkill(player, 9752, 1, true);
							player.getSkillList().addSkill(player, 9755, 1, true);
							player.getSkillList().addSkill(player, 9756, 1, true);
							player.getSkillList().addSkill(player, 9757, 1, true);
							player.getSkillList().addSkill(player, 9758, 1, true);
							break;
					}
			}
			else //if race == ASMODIANS
			{
				switch( rank.getRank().getId() )
				{
					case 14:	//
						player.getSkillList().addSkill(player, 9742, 1, true);
						player.getSkillList().addSkill(player, 9747, 1, true);
						break;
					case 15:
						player.getSkillList().removeSkill(player, 9742);
						player.getSkillList().removeSkill(player, 9747);
						player.getSkillList().addSkill(player, 9743, 1, true);
						player.getSkillList().addSkill(player, 9748, 1, true);
						player.getSkillList().addSkill(player, 9753, 1, true);
						break;
					case 16:
						player.getSkillList().removeSkill(player, 9743);
						player.getSkillList().removeSkill(player, 9748);
						player.getSkillList().removeSkill(player, 9753);
						player.getSkillList().addSkill(player, 9744, 1, true);
						player.getSkillList().addSkill(player, 9749, 1, true);
						player.getSkillList().addSkill(player, 9753, 1, true);
						player.getSkillList().addSkill(player, 9755, 1, true);
						break;
					case 17:
						player.getSkillList().removeSkill(player, 9744);
						player.getSkillList().removeSkill(player, 9749);
						player.getSkillList().removeSkill(player, 9753);
						player.getSkillList().removeSkill(player, 9755);
						player.getSkillList().addSkill(player, 9745, 1, true);
						player.getSkillList().addSkill(player, 9750, 1, true);
						player.getSkillList().addSkill(player, 9754, 1, true);
						player.getSkillList().addSkill(player, 9755, 1, true);
						player.getSkillList().addSkill(player, 9756, 1, true);
						break;
					case 18:
						player.getSkillList().removeSkill(player, 9745);
						player.getSkillList().removeSkill(player, 9750);
						player.getSkillList().removeSkill(player, 9754);
						player.getSkillList().removeSkill(player, 9755);
						player.getSkillList().removeSkill(player, 9756);
						player.getSkillList().addSkill(player, 9746, 1, true);
						player.getSkillList().addSkill(player, 9750, 1, true);
						player.getSkillList().addSkill(player, 9754, 1, true);
						player.getSkillList().addSkill(player, 9755, 1, true);
						player.getSkillList().addSkill(player, 9756, 1, true);
						player.getSkillList().addSkill(player, 9757, 1, true);
						player.getSkillList().addSkill(player, 9758, 1, true);
						break;
				}
			}
		}
		else //abyss rank decrease, remove abyss skills
		{
			if (this.getRace().getRaceId()==0) //ELYOS
			{
				switch( rank.getRank().getId() )
				{
					case 17:
						player.getSkillList().removeSkill(player, 9741);
						player.getSkillList().removeSkill(player, 9750);
						player.getSkillList().removeSkill(player, 9752);
						player.getSkillList().removeSkill(player, 9755);
						player.getSkillList().removeSkill(player, 9756);
						player.getSkillList().removeSkill(player, 9757);
						player.getSkillList().removeSkill(player, 9758);
						player.getSkillList().addSkill(player, 9740, 1, true);
						player.getSkillList().addSkill(player, 9750, 1, true);
						player.getSkillList().addSkill(player, 9752, 1, true);
						player.getSkillList().addSkill(player, 9755, 1, true);
						player.getSkillList().addSkill(player, 9756, 1, true);
						break;
					case 16:
						player.getSkillList().removeSkill(player, 9738);
						player.getSkillList().removeSkill(player, 9748);
						player.getSkillList().removeSkill(player, 9751);
						player.getSkillList().addSkill(player, 9739, 1, true);
						player.getSkillList().addSkill(player, 9749, 1, true);
						player.getSkillList().addSkill(player, 9751, 1, true);
						player.getSkillList().addSkill(player, 9755, 1, true);
						break;
					case 15:
						player.getSkillList().removeSkill(player, 9737);
						player.getSkillList().removeSkill(player, 9747);
						player.getSkillList().addSkill(player, 9738, 1, true);
						player.getSkillList().addSkill(player, 9748, 1, true);
						player.getSkillList().addSkill(player, 9751, 1, true);
						break;
					case 14:	//
						player.getSkillList().removeSkill(player, 9738);
						player.getSkillList().removeSkill(player, 9748);
						player.getSkillList().removeSkill(player, 9751);
						player.getSkillList().addSkill(player, 9737, 1, true);
						player.getSkillList().addSkill(player, 9747, 1, true);
						break;
					case 13:
						player.getSkillList().removeSkill(player, 9737);
						player.getSkillList().removeSkill(player, 9747);
						break;
				}
			}
			else //ASMODIANS
			{
				switch( rank.getRank().getId() )
				{
					case 17:
						player.getSkillList().removeSkill(player, 9745);
						player.getSkillList().removeSkill(player, 9750);
						player.getSkillList().removeSkill(player, 9754);
						player.getSkillList().removeSkill(player, 9755);
						player.getSkillList().removeSkill(player, 9756);
						player.getSkillList().addSkill(player, 9745, 1, true);
						player.getSkillList().addSkill(player, 9750, 1, true);
						player.getSkillList().addSkill(player, 9754, 1, true);
						player.getSkillList().addSkill(player, 9755, 1, true);
						player.getSkillList().addSkill(player, 9756, 1, true);
						break;
					case 16:
						player.getSkillList().removeSkill(player, 9745);
						player.getSkillList().removeSkill(player, 9750);
						player.getSkillList().removeSkill(player, 9754);
						player.getSkillList().removeSkill(player, 9755);
						player.getSkillList().removeSkill(player, 9756);
						player.getSkillList().addSkill(player, 9744, 1, true);
						player.getSkillList().addSkill(player, 9749, 1, true);
						player.getSkillList().addSkill(player, 9753, 1, true);
						player.getSkillList().addSkill(player, 9755, 1, true);
						break;
					case 15:
						player.getSkillList().removeSkill(player, 9744);
						player.getSkillList().removeSkill(player, 9749);
						player.getSkillList().removeSkill(player, 9753);
						player.getSkillList().removeSkill(player, 9755);
						player.getSkillList().addSkill(player, 9743, 1, true);
						player.getSkillList().addSkill(player, 9748, 1, true);
						player.getSkillList().addSkill(player, 9753, 1, true);
						break;
					case 14:	//
						player.getSkillList().removeSkill(player, 9743);
						player.getSkillList().removeSkill(player, 9748);
						player.getSkillList().removeSkill(player, 9753);
						player.getSkillList().addSkill(player, 9742, 1, true);
						player.getSkillList().addSkill(player, 9747, 1, true);
						break;
					case 13:
						player.getSkillList().removeSkill(player, 9742);
						player.getSkillList().removeSkill(player, 9747);
						break;
				}
			}
		}
		
		PacketSendUtility.sendPacket(player, new SM_ABYSS_RANK(player.getAbyssRank()));
	}
	
	
	public Race getRace()
	{
		return race;
	}

	public void setRace(Race race)
	{
		this.race = race;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public PlayerClass getPlayerClass()
	{
		return playerClass;
	}

	public void setPlayerClass(PlayerClass playerClass)
	{
		this.playerClass = playerClass;
	}

	public boolean isOnline() 
	{
		return online;
	}
	public void setOnline(boolean online)
	{
		this.online = online;
	}

	public Gender getGender()
	{
		return gender;
	}

	public void setGender(Gender gender)
	{
		this.gender = gender;
	}

	public WorldPosition getPosition()
	{
		return position;
	}

	public Timestamp getLastOnline()
	{
		return lastOnline;
	}

	public void setBindPoint(int bindId)
	{
		this.bindPoint = bindId;
	}

	public int getBindPoint()
	{
		return bindPoint;
	}

	public void setLastOnline(Timestamp timestamp)
	{
		lastOnline = timestamp;
	}

	public int getLevel()
	{
		return level;
	}

	public void setLevel(int level)
	{
		if (level <= DataManager.PLAYER_EXPERIENCE_TABLE.getMaxLevel())
		{
			this.setExp(DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(level));
		}
	}

	public String getNote()
	{
		return note;
	}

	public void setNote(String note)
	{
		this.note = note;
	}

	public int getTitleId()
	{
		return titleId;
	}

	public void setTitleId(int titleId)
	{
		this.titleId = titleId;
	}

	/**
	 * This method should be called exactly once after creating object of this class
	 * @param position
	 */
	public void setPosition(WorldPosition position)
	{
		if(this.position != null)
		{
			throw new IllegalStateException("position already set");
		}
		this.position = position;
	}

	/**
	 * Gets the cooresponding Player for this common data.
	 * Returns null if the player is not online
	 * @return Player or null
	 */
	public Player getPlayer()
	{
		if (online && getPosition() != null)
		{
			return World.getInstance().findPlayer(playerObjId);
		}
		return null;
	}

	public void addDp(int dp)
	{
		setDp(this.dp + dp);
	}

	/**
	 *  //TODO move to lifestats -> db save?
	 *  
	 * @param dp
	 */
	public void setDp(int dp)
	{
		if(getPlayer() != null)
		{
			if(playerClass.isStartingClass())
				return;
			
			int maxDp = getPlayer().getGameStats().getCurrentStat(StatEnum.MAXDP);
			this.dp = dp > maxDp ? maxDp : dp;

			PacketSendUtility.broadcastPacket(getPlayer(), new SM_DP_INFO(playerObjId, this.dp), true);
			PacketSendUtility.sendPacket(getPlayer(), new SM_STATS_INFO(getPlayer()));
			PacketSendUtility.sendPacket(getPlayer(), new SM_STATUPDATE_DP(this.dp));
		}
		else
		{
			log.warn("CHECKPOINT : getPlayer in PCD return null for setDP " + isOnline() + " " + getPosition());
		}
	}

	public int getDp()
	{
		return this.dp;
	}

	@Override
	public int getTemplateId()
	{
		return 100000 + race.getRaceId()*2 + gender.getGenderId();
	}

	@Override
	public int getNameId()
	{
		return 0;
	}

	/**
	 * @param warehouseSize the warehouseSize to set
	 */
	public void setWarehouseSize(int warehouseSize)
	{
		this.warehouseSize = warehouseSize;
	}

	/**
	 * @return the warehouseSize
	 */
	public int getWarehouseSize()
	{
		return warehouseSize;
	}
	
	public void setMailboxLetters(int count)
	{
		this.mailboxLetters = count;
	}
	
	public int getMailboxLetters()
	{
		return mailboxLetters;
	}
}
