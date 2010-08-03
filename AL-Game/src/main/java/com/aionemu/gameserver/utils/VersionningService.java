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
package com.aionemu.gameserver.utils;

import java.util.Date;

import org.apache.log4j.Logger;

import com.aionemu.commons.utils.AEInfos;
import com.aionemu.commons.versionning.Version;
import com.aionemu.gameserver.GameServer;

/**
 * @author lord_rex
 *    l2j-free versionning for Maven, thanks Noctarius
 */
public class VersionningService
{
	private static final Logger		log			= Logger.getLogger(VersionningService.class);
	
	private static final VersionInfo	commons		= new VersionInfo(AEInfos.class);
	private static final VersionInfo	game		= new VersionInfo(GameServer.class);
	
	public static String getCommonsVersion()
	{
		return commons.getBuildVersion();
	}
	
	public static String getGameVersion()
	{
		return game.getBuildVersion();
	}
	
	public static String getCommonsRevision()
	{
		return commons.getBuildRevision();
	}
	
	public static String getGameRevision()
	{
		return game.getBuildRevision();
	}
	
	public static Date getCommonsDate()
	{
		return commons.getBuildDate();
	}
	
	public static Date getGameDate()
	{
		return game.getBuildDate();
	}
	
	private static final class VersionInfo extends Version
	{
		private final String	version;
		private final String	revision;
		private final Date		buildDate;

		public VersionInfo(Class<?> c)
		{
			super(c);
			
			this.version = String.format("%-6s", getVersion());
			this.revision = String.format("%-6s", getRevision());
			this.buildDate = new Date(getDate());
		}

		public String getBuildVersion() 
		{
			return version;
		}

		public String getBuildRevision() 
		{
			return revision;
		}
		
		public Date getBuildDate() 
		{
			return buildDate;
		}
	}

	public static String[] getFullVersionInfo()
	{
		return new String[] { 
			"Commons Version: " + getCommonsVersion(),
			"Commons Revision: " + getCommonsRevision(),
			"Commons Build Date: " + getCommonsDate(), 
			"GS Version: " + getGameVersion(),
			"GS Revision: " + getGameRevision(),
			"GS Build Date: " + getGameDate(),
		};
	}

	public static void printFullVersionInfo()
	{
		for(String line : getFullVersionInfo())
			log.info(line);
	}
}
