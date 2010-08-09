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
import com.aionemu.gameserver.configs.Config;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.TaskManagerConfig;
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
import com.aionemu.gameserver.services.PeriodicSaveService;
import com.aionemu.gameserver.services.PetitionService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.WeatherService;
import com.aionemu.gameserver.services.ZoneService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.taskmanager.TaskManagerFromDB;
import com.aionemu.gameserver.taskmanager.tasks.PacketBroadcaster;
import com.aionemu.gameserver.utils.DeadlockDetector;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.ThreadUncaughtExceptionHandler;
import com.aionemu.gameserver.utils.Util;
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
	/** Logger for gameserver */
	private static final Logger	log	= Logger.getLogger(GameServer.class);

	/**
	 * Launching method for GameServer
	 * 
	 * @param args
	 *            arguments, not used
	 */
	public static void main(String[] args)
	{
		long start = System.currentTimeMillis();

		initUtilityServicesAndConfig();
		
		Util.printSection("World");
		DataManager.getInstance();
		IDFactory.getInstance();
		World.getInstance();

		GameServer gs = new GameServer();
		// Set all players is offline
		DAOManager.getDAO(PlayerDAO.class).setPlayersOffline(false);

		Util.printSection("Spawns");
		SpawnEngine.getInstance();

		Util.printSection("Quests");
		QuestEngine.getInstance();
		QuestEngine.getInstance().load();

		Util.printSection("TaskManagers");
		PacketBroadcaster.getInstance();
	
		GameTimeService.getInstance();

		AnnouncementService.getInstance();

		DebugService.getInstance();

		ZoneService.getInstance();
		
		WeatherService.getInstance();

		DuelService.getInstance();

		MailService.getInstance();

		GroupService.getInstance();
		
		AllianceService.getInstance();
		
		BrokerService.getInstance();

		SiegeService.getInstance();
		
		Influence.getInstance();
		
		DropService.getInstance();

		ExchangeService.getInstance();

		PeriodicSaveService.getInstance();
		
		PetitionService.getInstance();

		ChatHandlers.getInstance();
		
		TaskManagerFromDB.getInstance();

		Util.printSection("System");
		VersionningService.printFullVersionInfo();
		System.gc();
		AEInfos.printAllInfos();

		Util.printSection("GameServerLog");
		log.info("AE Game Server started in " + (System.currentTimeMillis() - start) / 1000 + " seconds.");

		gs.startServers();
		GameTimeManager.startClock();

		if(TaskManagerConfig.DEADLOCK_DETECTOR_ENABLED)
		{
			log.info("Starting deadlock detector");
			new Thread(new DeadlockDetector(TaskManagerConfig.DEADLOCK_DETECTOR_INTERVAL)).start();
		}

		Runtime.getRuntime().addShutdownHook(ShutdownHook.getInstance());

		onStartup();
	}

	/**
	 * Starts servers for connection with aion client and login server.
	 */
	private void startServers()
	{	
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
	}

	/**
	 * Initialize all helper services, that are not directly related to aion gs, which includes:
	 * <ul>
	 * <li>Logging</li>
	 * <li>Database factory</li>
	 * <li>Thread pool</li>
	 * </ul>
	 * 
	 * This method also initializes {@link Config}
	 * 
	 * @throws Log4jInitializationError
	 */
	private static void initUtilityServicesAndConfig() throws Log4jInitializationError
	{
		// Set default uncaught exception handler
		Thread.setDefaultUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());
		// First of all we must initialize logging
		LoggingService.init();
		// init config
		Config.load();
		// Second should be database factory
		Util.printSection("DataBase");
		DatabaseFactory.init();
		// Initialize DAOs
		DAOManager.init();
		// Initialize thread pools
		Util.printSection("Threads");
		ThreadConfig.load();
		ThreadPoolManager.getInstance();
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
