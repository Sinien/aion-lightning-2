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

public class OptionsConfig
{
	/**
	 * Enable/disable deadlock detector
	 */
	@Property(key = "detectors.deadlock.enabled", defaultValue = "true")
	public static boolean	DEADLOCK_DETECTOR_ENABLED;

	/**
	 * Interval for deadlock detector run schedule
	 */
	@Property(key = "detectors.deadlock.interval", defaultValue = "300")
	public static int		DEADLOCK_DETECTOR_INTERVAL;

	@Property(key = "player.tasks.general", defaultValue = "900")
	public static int		PLAYER_GENERAL;

	@Property(key = "tasks.items", defaultValue = "60")
	public static int		ITEMS;

	@Property(key = "world.region.size", defaultValue = "500")
	public static int		REGION_SIZE;

	@Property(key = "world.region.maxsize", defaultValue = "10000")
	public static int		MAX_WORLD_SIZE;
	
	@Property(key = "log.audit", defaultValue = "true")
	public static boolean	LOG_AUDIT;

	@Property(key = "log.chat", defaultValue = "true")
	public static boolean	LOG_CHAT;
	
	@Property(key = "log.gmaudit", defaultValue = "true")
	public static boolean	LOG_GMAUDIT;

	@Property(key = "log.item", defaultValue = "true")
	public static boolean	LOG_ITEM;
	
	@Property(key = "log.packets", defaultValue = "true")
	public static boolean	LOG_PACKETS;
}
