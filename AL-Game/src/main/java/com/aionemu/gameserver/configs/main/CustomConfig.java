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
package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

public class CustomConfig
{
	/**
	 * Factions speaking mode
	 */
	@Property(key = "gameserver.factions.speaking.mode", defaultValue = "0")
	public static int		FACTIONS_SPEAKING_MODE;

	/*
	* Factions search mode
	*/
	@Property(key = "gameserver.factions.search.mode", defaultValue = "false")
	public static boolean	FACTIONS_SEARCH_MODE;

	/**
	 * Skill autolearn
	 */
	@Property(key = "gameserver.skill.autolearn", defaultValue = "false")
	public static boolean	SKILL_AUTOLEARN;

	/**
	 * Disable monsters aggressive behave
	 */
	@Property(key = "gameserver.disable.mob.aggro", defaultValue = "false")
	public static boolean	DISABLE_MOB_AGGRO;

	/**
	 * Enable 2nd class change simple mode
	 */
	@Property(key = "gameserver.enable.simple.2ndclass", defaultValue = "false")
	public static boolean	ENABLE_SIMPLE_2NDCLASS;

	/**
	 * Unstuck delay
	 */
	@Property(key = "gameserver.unstuck.delay", defaultValue = "3600")
	public static int		UNSTUCK_DELAY;

	/**
	 * Enable instances
	 */
	@Property(key = "gameserver.instances.enable", defaultValue = "true")
	public static boolean	ENABLE_INSTANCES;
	
	/**
	 * Base Fly Time
	 */
	@Property(key = "gameserver.base.flytime", defaultValue = "60")
	public static int		BASE_FLYTIME;

    /**
	* Allows players of opposite factions to bind in enemy territory
	*/
	@Property(key = "gameserver.cross.faction.binding", defaultValue = "false")
	public static boolean				ENABLE_CROSS_FACTION_BINDING;
	
	/**
	* Disable drop rate reduction based on level diference between players and mobs
	*/
	@Property(key = "gameserver.disable.drop.reduction", defaultValue = "false")
	public static boolean				DISABLE_DROP_REDUCTION;
	
	/**
	* Allowed Kills in 24 hours for full AP. Move to separate config when more pvp options. 
	*/
	@Property(key = "gameserver.pvp.maxkills", defaultValue = "5")
	public static int					MAX_DAILY_PVP_KILLS;
	
}
