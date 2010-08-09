/*
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
package com.aionemu.gameserver;

import org.apache.log4j.Logger;

import com.aionemu.commons.utils.ExitCode;
import com.aionemu.commons.utils.concurrent.RunnableStatsManager;
import com.aionemu.commons.utils.concurrent.RunnableStatsManager.SortBy;
import com.aionemu.gameserver.configs.main.ShutdownConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.services.PlayerService;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.gametime.GameTimeManager;
import com.aionemu.gameserver.world.World;

/**
 * @author lord_rex
 * 
 */
public class ShutdownHook extends Thread
{
	private static final Logger	log	= Logger.getLogger(ShutdownHook.class);

	public static ShutdownHook getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	@Override
	public void run()
	{
		if(ShutdownConfig.HOOK_MODE == 1)
		{
			shutdownHook(ShutdownConfig.HOOK_DELAY, ShutdownConfig.ANNOUNCE_INTERVAL, ShutdownMode.SHUTDOWN);
		}
		else if(ShutdownConfig.HOOK_MODE == 2)
		{
			shutdownHook(ShutdownConfig.HOOK_DELAY, ShutdownConfig.ANNOUNCE_INTERVAL, ShutdownMode.RESTART);
		}
	}

	public static enum ShutdownMode
	{
		NONE("terminating"),
		SHUTDOWN("shutting down"),
		RESTART("restarting");

		private final String	text;

		private ShutdownMode(String text)
		{
			this.text = text;
		}

		public String getText()
		{
			return text;
		}
	}

	private void sendShutdownMessage(int seconds)
	{
		try
		{
			for(Player player : World.getInstance().getAllPlayers())
			{
				if(player != null && player.getClientConnection() != null)
					player.getClientConnection().sendPacket(SM_SYSTEM_MESSAGE.SERVER_SHUTDOWN(seconds));
			}
		}
		catch(Exception e)
		{
			log.error(e.getMessage());
		}
	}

	private void sendShutdownStatus(boolean status)
	{
		try
		{
			for(Player player : World.getInstance().getAllPlayers())
			{
				if(player != null && player.getClientConnection() != null)
					player.getController().setInShutdownProgress(status);
			}
		}
		catch(Exception e)
		{
			log.error(e.getMessage());
		}
	}

	private void shutdownHook(int duration, int interval, ShutdownMode mode)
	{
		for(int i = duration; i >= interval; i -= interval)
		{
			try
			{
				if(World.getInstance().getAllPlayers().size() == 0)
				{
					log.info("Runtime is " + mode.getText() + " now ...");
					break; // fast exit.
				}
				
				log.info("Runtime is " + mode.getText() + " in " + i + " seconds.");
				sendShutdownMessage(i);
				sendShutdownStatus(ShutdownConfig.SAFE_REBOOT);

				if(i > interval)
				{
					sleep(interval * 1000);
				}
				else
				{
					sleep(1000);
				}
			}
			catch(InterruptedException e)
			{
				return;
			}
		}

		// Disconnect login server from game.
		LoginServer.getInstance().gameServerDisconnected();

		// Disconnect all players.
		for(Player player : World.getInstance().getAllPlayers())
		{
			try
			{
				PlayerService.playerLoggedOut(player);
			}
			catch(Exception e)
			{
				log.error("Error while saving player " + e.getMessage());
			}
		}
		log.info("All players are disconnected...");
		
		RunnableStatsManager.dumpClassStats(SortBy.AVG);

		// Save game time.
		GameTimeManager.saveTime();
		// ThreadPoolManager shutdown
		ThreadPoolManager.getInstance().shutdown();

		// Do system exit.
		if(mode == ShutdownMode.RESTART)
			Runtime.getRuntime().halt(ExitCode.CODE_RESTART);
		else
			Runtime.getRuntime().halt(ExitCode.CODE_NORMAL);

		log.info("Runtime is " + mode.getText() + " now...");
	}

	/**
	 * 
	 * @param delay
	 * @param announceInterval
	 * @param mode
	 */
	public void doShutdown(int delay, int announceInterval, ShutdownMode mode)
	{
		shutdownHook(delay, announceInterval, mode);
	}

	private static final class SingletonHolder
	{
		private static final ShutdownHook INSTANCE = new ShutdownHook();
	}
}
