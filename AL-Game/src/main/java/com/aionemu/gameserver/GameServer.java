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
package com.aionemu.gameserver;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.log4j.exceptions.Log4jInitializationError;
import com.aionemu.commons.network.NioServer;
import com.aionemu.commons.network.ServerCfg;
import com.aionemu.commons.services.LoggingService;
import com.aionemu.commons.utils.AEInfos;
import com.aionemu.gameserver.cache.HTMLCache;
import com.aionemu.gameserver.configs.Config;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.OptionsConfig;
import com.aionemu.gameserver.configs.main.ThreadConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.siege.Influence;
import com.aionemu.gameserver.network.aion.GameConnectionFactoryImpl;
import com.aionemu.gameserver.network.chatserver.ChatServer;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.services.AllianceService;
import com.aionemu.gameserver.services.AnnouncementService;
import com.aionemu.gameserver.services.BrokerService;
import com.aionemu.gameserver.services.DebugService;
import com.aionemu.gameserver.services.DropService;
import com.aionemu.gameserver.services.DuelService;
import com.aionemu.gameserver.services.ExchangeService;
import com.aionemu.gameserver.services.GameTimeService;
import com.aionemu.gameserver.services.GroupService;
import com.aionemu.gameserver.services.MailService;
import com.aionemu.gameserver.services.PetitionService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.WeatherService;
import com.aionemu.gameserver.services.ZoneService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.taskmanager.TaskManagerFromDB;
import com.aionemu.gameserver.taskmanager.tasks.ItemUpdater;
import com.aionemu.gameserver.taskmanager.tasks.PacketBroadcaster;
import com.aionemu.gameserver.utils.DeadlockDetector;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.ThreadUncaughtExceptionHandler;
import com.aionemu.gameserver.utils.VersionningService;
import com.aionemu.gameserver.utils.chathandlers.ChatHandlers;
import com.aionemu.gameserver.utils.gametime.GameTimeManager;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;

/**
 * <tt>GameServer</tt> is the main class of the application and represents the whole game server.<br>
 * This class is also an entry point with main() method.
 * 
 * @author -Nemesiss-
 * @author SoulKeeper
 */
public class GameServer
{
	private static final Logger	log	= Logger.getLogger(GameServer.class);
	
	/**
	 * Launching method for GameServer
	 * 
	 * @param args
	 *            arguments, not used
	 */
	public static void main(String[] args)
	{
		new GameServer();
	}

	public GameServer() throws Log4jInitializationError
	{
		long start = System.currentTimeMillis();

		// Set default uncaught exception handler
		Thread.setDefaultUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());
		
		// First of all we must initialize logging
		LoggingService.init();
		log.info("Logging Initialized.");
		
		// init config
		Config.load();
		
		// Second should be database factory
		AEInfos.printSection("DataBase");
		DatabaseFactory.init();
		// Initialize DAOs
		DAOManager.init();
		
		// Initialize thread pools
		AEInfos.printSection("Threads");
		ThreadConfig.load();
		ThreadPoolManager.getInstance();
		
		AEInfos.printSection("StaticDatas");
		DataManager.getInstance();
		
		AEInfos.printSection("IDFactory");
		IDFactory.getInstance();
		
		AEInfos.printSection("World");
		World.getInstance();

		// Set all players is offline
		DAOManager.getDAO(PlayerDAO.class).setPlayersOffline(false);
		
		AEInfos.printSection("TaskManagers");
		PacketBroadcaster.getInstance();
		ItemUpdater.getInstance();
		TaskManagerFromDB.getInstance();
		
		AEInfos.printSection("Drops");
		DropService.getInstance();

		AEInfos.printSection("Spawns");
		SpawnEngine.getInstance();

		AEInfos.printSection("Quests");
		QuestEngine.getInstance();
		QuestEngine.getInstance().load();
	
		AEInfos.printSection("Time");
		GameTimeService.getInstance();

		AEInfos.printSection("Announcements");
		AnnouncementService.getInstance();
		
		AEInfos.printSection("Debug");
		DebugService.getInstance();
		
		AEInfos.printSection("Zones");
		ZoneService.getInstance();		
		
		AEInfos.printSection("Weather");
		WeatherService.getInstance();
		
		AEInfos.printSection("Duel");
		DuelService.getInstance();
		
		AEInfos.printSection("Mail");
		MailService.getInstance();
		
		AEInfos.printSection("Group");
		GroupService.getInstance();		
		
		AEInfos.printSection("Alliance");
		AllianceService.getInstance();
		
		AEInfos.printSection("Broker");
		BrokerService.getInstance();
		
		AEInfos.printSection("Sieges");
		SiegeService.getInstance();	
		Influence.getInstance();	
		
		AEInfos.printSection("Exchange");
		ExchangeService.getInstance();	
		
		AEInfos.printSection("Petitions");
		PetitionService.getInstance();
		
		AEInfos.printSection("ChatHandlers");
		ChatHandlers.getInstance();
		
		AEInfos.printSection("HTMLs");
		HTMLCache.getInstance();

		AEInfos.printSection("System");
		VersionningService.printFullVersionInfo();
		System.gc();
		System.runFinalization();
		AEInfos.printAllInfos();

		AEInfos.printSection("IOServer");
		ServerCfg aion = new ServerCfg(NetworkConfig.GAME_BIND_ADDRESS, NetworkConfig.GAME_PORT, "Game Connections", new GameConnectionFactoryImpl());
		NioServer nioServer = new NioServer(1, ThreadPoolManager.getInstance(), aion);

		LoginServer loginServer = LoginServer.getInstance();
		ChatServer chatServer = ChatServer.getInstance();
		loginServer.setNioServer(nioServer);
		chatServer.setNioServer(nioServer);
		
		// Nio must go first
		nioServer.connect();
		loginServer.connect();
		if(!GSConfig.DISABLE_CHAT_SERVER)
			chatServer.connect();
		
		GameTimeManager.startClock();

		if(OptionsConfig.DEADLOCK_DETECTOR_ENABLED)
		{
			AEInfos.printSection("DeadLock Detector");
			new Thread(new DeadlockDetector(OptionsConfig.DEADLOCK_DETECTOR_INTERVAL)).start();
		}

		Runtime.getRuntime().addShutdownHook(ShutdownHook.getInstance());
		
		AEInfos.printSection("GameServerLog");
		log.info("Total Boot Time: " + (System.currentTimeMillis() - start) / 1000 + " seconds.");

		onStartup();
	}

	private static Set<StartupHook>	startUpHooks	= new HashSet<StartupHook>();

	public synchronized static void addStartupHook(StartupHook hook)
	{
		if(startUpHooks != null)
			startUpHooks.add(hook);
		else
			hook.onStartup();
	}

	private synchronized static void onStartup()
	{
		final Set<StartupHook> startupHooks = startUpHooks;

		startUpHooks = null;

		for(StartupHook hook : startupHooks)
			hook.onStartup();
	}

	public interface StartupHook
	{
		public void onStartup();
	}
}
