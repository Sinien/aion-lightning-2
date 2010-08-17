/*
 * This file is part of aion-emu <aion-emu.com>.
 *
 * aion-emu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aion-emu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with aion-emu.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.commons.database;

import java.io.File;

import com.aionemu.commons.configuration.Property;

/**
 * This class holds all configuration of database
 * 
 * @author SoulKeeper
 */
public class DatabaseConfig
{
	/**
	 * Default database url.
	 */
	@Property(key = "database.url", defaultValue = "jdbc:mysql://localhost:3306/Aion_DB")
	public static String		DATABASE_URL;

	/**
	 * Name of database Driver
	 */
	@Property(key = "database.driver", defaultValue = "com.mysql.jdbc.Driver")
	public static Class<?>		DATABASE_DRIVER;

	/**
	 * Default database user
	 */
	@Property(key = "database.user", defaultValue = "root")
	public static String		DATABASE_USER;

	/**
	 * Default database password
	 */
	@Property(key = "database.password", defaultValue = "root")
	public static String		DATABASE_PASSWORD;

	/**
	 * Minimum amount of connections that are always active
	 */
	@Property(key = "database.connections.min", defaultValue = "2")
	public static int			DATABASE_CONNECTIONS_MIN;

	/**
	 * Maximum amount of connections that are allowed to use
	 */
	@Property(key = "database.connections.max", defaultValue = "10")
	public static int			DATABASE_CONNECTIONS_MAX;

	/**
	 * Location of database script context descriptor
	 */
	@Property(key = "database.scriptcontext.descriptor", defaultValue = "./data/scripts/system/database/database.xml")
	public static File			DATABASE_SCRIPTCONTEXT_DESCRIPTOR;
}
