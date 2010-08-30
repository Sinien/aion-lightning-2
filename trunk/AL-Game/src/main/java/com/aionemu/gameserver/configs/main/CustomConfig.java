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
	@Property(key = "factions.speaking.mode", defaultValue = "0")
	public static int		FACTIONS_SPEAKING_MODE;

	/*
	 * Factions search mode
	 */
	@Property(key = "factions.search.mode", defaultValue = "false")
	public static boolean	FACTIONS_SEARCH_MODE;

	/**
	 * Skill autolearn
	 */
	@Property(key = "skill.autolearn", defaultValue = "false")
	public static boolean	SKILL_AUTOLEARN;

	/**
	 * Disable monsters aggressive behave
	 */
	@Property(key = "disable.mob.aggro", defaultValue = "false")
	public static boolean	DISABLE_MOB_AGGRO;

	/**
	 * Enable 2nd class change simple mode
	 */
	@Property(key = "enable.simple.2ndclass", defaultValue = "false")
	public static boolean	ENABLE_SIMPLE_2NDCLASS;

	/**
	 * Unstuck delay
	 */
	@Property(key = "unstuck.delay", defaultValue = "3600")
	public static int		UNSTUCK_DELAY;

	/**
	 * Enable instances
	 */
	@Property(key = "instances.enable", defaultValue = "true")
	public static boolean	ENABLE_INSTANCES;

	/**
	 * Base Fly Time
	 */
	@Property(key = "base.flytime", defaultValue = "60")
	public static int		BASE_FLYTIME;

	/**
	 * Allows players of opposite factions to bind in enemy territory
	 */
	@Property(key = "cross.faction.binding", defaultValue = "false")
	public static boolean	ENABLE_CROSS_FACTION_BINDING;

	/**
	 * Disable drop rate reduction based on level diference between players and mobs
	 */
	@Property(key = "disable.drop.reduction", defaultValue = "false")
	public static boolean	DISABLE_DROP_REDUCTION;

	/**
	 * Allowed Kills in 24 hours for full AP. Move to separate config when more pvp options.
	 */
	@Property(key = "pvp.maxkills", defaultValue = "5")
	public static int		MAX_DAILY_PVP_KILLS;

	/**
	 * Enable the HTML Welcome Message Window on Player Login
	 */

	@Property(key = "enable.html.welcome", defaultValue = "false")
	public static boolean	ENABLE_HTML_WELCOME;

}
