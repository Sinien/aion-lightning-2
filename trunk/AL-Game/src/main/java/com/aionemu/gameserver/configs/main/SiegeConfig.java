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

/**
 * @author Sarynth
 */
public class SiegeConfig
{
	/**
	 * Basic Siege Config
	 */

	/**
	 * Siege Enabled
	 */
	@Property(key = "gameserver.siege.enabled", defaultValue = "true")
	public static boolean	SIEGE_ENABLED;

	/**
	 * Siege Timer Interval
	 */
	@Property(key = "gameserver.siege.interval", defaultValue = "7200")
	public static int		SIEGE_TIMER_INTERVAL;

	/**
	 * Siege Location Values
	 */

	@Property(key = "gameserver.siege.influence.fortress", defaultValue = "10")
	public static int		SIEGE_POINTS_FORTRESS;

	@Property(key = "gameserver.siege.influence.artifact", defaultValue = "1")
	public static int		SIEGE_POINTS_ARTIFACT;

}
