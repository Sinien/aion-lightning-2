/*
 * This file is part of aion-unique <aion-unique.com>.
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

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.PersistentState;

/**
 * @author ATracer
 *
 */
public class SkillListEntry
{
	private int skillId;
	
	private int skillLvl;
	
	private boolean isStigma;
	
	/**
	 * for crafting skills
	 */
	private int currentXp;
	
	private PersistentState persistentState;

	public SkillListEntry(int skillId, boolean isStigma, int skillLvl, PersistentState persistentState)
	{
		this.skillId = skillId;
		this.skillLvl = skillLvl;
		this.isStigma = isStigma;
		this.persistentState = persistentState;
	}

	/**
	 * @return the skillId
	 */
	public int getSkillId()
	{
		return skillId;
	}

	/**
	 * @return the skillLvl
	 */
	public int getSkillLevel()
	{
		return skillLvl;
	}
	
	/**
	 * 
	 * @return isStigma
	 */
	public boolean isStigma()
	{
		return this.isStigma;
	}
	
	/**
	 * 
	 * @return the skill name
	 */
	public String getSkillName()
	{
		return DataManager.SKILL_DATA.getSkillTemplate(skillId).getName();
	}

	/**
	 * @param skillLvl the skillLvl to set
	 */
	public void setSkillLvl(int skillLvl)
	{
		this.skillLvl = skillLvl;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * @return The skill extra lvl
	 */
	public int getExtraLvl()
	{
		switch(skillId)
		{
			case 30002:
			case 30003:
			case 40001:
			case 40002:
			case 40003:
			case 40004:
			case 40007:
			case 40008:
				return skillLvl/100;
		}
		return 0;
	}
	/**
	 * @return the currentXp
	 */
	public int getCurrentXp()
	{
		return currentXp;
	}

	/**
	 * @param currentXp the currentXp to set
	 */
	public void setCurrentXp(int currentXp)
	{
		this.currentXp = currentXp;
	}
	
	/**
	 *  
	 * @param xp
	 */
	public boolean addSkillXp(int xp)
	{
		this.currentXp += xp;
		if(currentXp > (0.15*(skillLvl+30)*(skillLvl+30)))
		{
			currentXp = 0;
			setSkillLvl(skillLvl + 1);
			return true;
		}
		return false;
	}

	/**
	 * @return the pState
	 */
	public PersistentState getPersistentState()
	{
		return persistentState;
	}

	/**
	 * @param persistentState the pState to set
	 */
	public void setPersistentState(PersistentState persistentState)
	{
		switch(persistentState)
		{
			case DELETED:
				if(this.persistentState == PersistentState.NEW)
					this.persistentState = PersistentState.NOACTION;
				else
					this.persistentState = PersistentState.DELETED;
				break;
			case UPDATE_REQUIRED:
				if(this.persistentState != PersistentState.NEW)
					this.persistentState = PersistentState.UPDATE_REQUIRED;
				break;
			default:
				this.persistentState = persistentState;
		}
	}
	
}
