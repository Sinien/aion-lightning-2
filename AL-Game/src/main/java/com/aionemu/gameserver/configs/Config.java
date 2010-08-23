/**
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
package com.aionemu.gameserver.configs;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.aionemu.commons.configuration.ConfigurableProcessor;
import com.aionemu.commons.database.DatabaseConfig;
import com.aionemu.commons.utils.AEInfos;
import com.aionemu.commons.utils.PropertiesUtils;
import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.main.CacheConfig;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.EnchantsConfig;
import com.aionemu.gameserver.configs.main.FallDamageConfig;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.configs.main.LegionConfig;
import com.aionemu.gameserver.configs.main.NpcMovementConfig;
import com.aionemu.gameserver.configs.main.PricesConfig;
import com.aionemu.gameserver.configs.main.RateConfig;
import com.aionemu.gameserver.configs.main.ShutdownConfig;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.configs.main.OptionsConfig;
import com.aionemu.gameserver.configs.main.ThreadConfig;
import com.aionemu.gameserver.configs.network.IPConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;

/**
 * @author -Nemesiss-
 * @author SoulKeeper
 */
public class Config
{
	protected static final Logger	log	= Logger.getLogger(Config.class);

	/**
	 * Initialize all configs in com.aionemu.gameserver.configs package
	 */
	public static void load()
	{
		try
		{
			Properties[] props = PropertiesUtils.loadAllFromDirectory("./config");

			ConfigurableProcessor.process(Config.class, props);

			// Administration
			AEInfos.printSection("Administration");
			Properties[] admin = PropertiesUtils.loadAllFromDirectory("./config/administration");

			ConfigurableProcessor.process(AdminConfig.class, admin);

			// Main
			AEInfos.printSection("Main");
			Properties[] main = PropertiesUtils.loadAllFromDirectory("./config/main");

			ConfigurableProcessor.process(LegionConfig.class, main);
			ConfigurableProcessor.process(RateConfig.class, main);
			ConfigurableProcessor.process(CacheConfig.class, main);
			ConfigurableProcessor.process(ShutdownConfig.class, main);
			ConfigurableProcessor.process(OptionsConfig.class, main);
			ConfigurableProcessor.process(GroupConfig.class, main);
			ConfigurableProcessor.process(CustomConfig.class, main);
			ConfigurableProcessor.process(EnchantsConfig.class, main);
			ConfigurableProcessor.process(FallDamageConfig.class, main);
			ConfigurableProcessor.process(GSConfig.class, main);
			ConfigurableProcessor.process(NpcMovementConfig.class, main);
			ConfigurableProcessor.process(PricesConfig.class, main);
			ConfigurableProcessor.process(SiegeConfig.class, main);
			ConfigurableProcessor.process(ThreadConfig.class, main);

			// Network
			AEInfos.printSection("Network");
			Properties[] network = PropertiesUtils.loadAllFromDirectory("./config/network");

			ConfigurableProcessor.process(NetworkConfig.class, network);
			ConfigurableProcessor.process(DatabaseConfig.class, network);
		}
		catch(Exception e)
		{
			log.fatal("Can't load gameserver configuration: ", e);
			throw new Error("Can't load gameserver configuration: ", e);
		}

		IPConfig.load();
	}
}