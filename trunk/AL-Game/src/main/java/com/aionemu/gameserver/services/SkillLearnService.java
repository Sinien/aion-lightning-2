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

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.SkillList;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_LIST;
import com.aionemu.gameserver.skillengine.model.learn.SkillLearnTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 *
 */
public class SkillLearnService
{
	/**
	 * 
	 * @param player
	 * @param isNewCharacter
	 */
	public static void addNewSkills(Player player, boolean isNewCharacter)
	{
		int level = player.getCommonData().getLevel();
		PlayerClass playerClass = player.getCommonData().getPlayerClass();
		Race playerRace = player.getCommonData().getRace();
		
		if(isNewCharacter)
		{
			player.setSkillList(new SkillList());
		}
		
		addSkills(player, level, playerClass, playerRace, isNewCharacter);
	}
	
	/**
	 *  Recursively check missing skills and add them to player
	 *  
	 * @param player
	 */
	public static void addMissingSkills(Player player)
	{
		int level = player.getCommonData().getLevel();
		PlayerClass playerClass = player.getCommonData().getPlayerClass();
		Race playerRace = player.getCommonData().getRace();
		
		for(int i = 0; i <= level; i++)
		{
			addSkills(player, i, playerClass, playerRace, false);
		}
		
		if(!playerClass.isStartingClass())
		{
			PlayerClass startinClass = PlayerClass.getStartingClassFor(playerClass);
			
			for(int i = 1; i < 10; i++)
			{
				addSkills(player, i, startinClass, playerRace, false);
			}

			if (player.getSkillList().getSkillEntry(30001) != null)
			{
				int skillLevel = player.getSkillList().getSkillLevel(30001);
				player.getSkillList().removeSkill(30001);
				PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(player));
				player.getSkillList().addSkill(player, 30002, skillLevel, true);
			}
		}	
		
	}
	
	/**
	 *  Adds skill to player according to the specified level, class and race
	 *  
	 * @param player
	 * @param level
	 * @param playerClass
	 * @param playerRace
	 * @param isNewCharacter
	 */
	private static void addSkills(Player player, int level, PlayerClass playerClass, Race playerRace, boolean isNewCharacter)
	{
		SkillLearnTemplate[] skillTemplates =
			DataManager.SKILL_TREE_DATA.getTemplatesFor(playerClass, level, playerRace);
		
		SkillList playerSkillList = player.getSkillList();
		
		for(SkillLearnTemplate template : skillTemplates)
		{
			if(!checkLearnIsPossible(playerSkillList, template))
				continue;
			
 			playerSkillList.addSkill(player, template.getSkillId(), template.getSkillLevel(), !isNewCharacter);
		}
	}
	
	/**
	 *  Check SKILL_AUTOLEARN property
	 *  Check skill already learned
	 *  Check skill template auto-learn attribute
	 *  
	 * @param playerSkillList
	 * @param template
	 * @return
	 */
	private static boolean checkLearnIsPossible(SkillList playerSkillList, SkillLearnTemplate template)
	{
		if (playerSkillList.isSkillPresent(template.getSkillId()))
			return true;

		if (!template.isStigma()&& CustomConfig.SKILL_AUTOLEARN)
			return true;
		
		if(template.isAutolearn())
			return true;
		
		return false;
	}
}
