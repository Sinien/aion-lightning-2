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
package com.aionemu.chatserver.service;

import com.aionemu.chatserver.configs.Config;
import com.aionemu.chatserver.network.gameserver.GsAuthResponse;
import com.aionemu.chatserver.network.netty.handler.GameChannelHandler;

/**
 * 
 * @author ATracer
 *
 */
public class GameServerService
{
	public static byte GAMESERVER_ID;
	/**
	 * 
	 * @param gameChannelHandler
	 * @param gameServerId
	 * @param defaultAddress
	 * @param password
	 * @return
	 */
	public static GsAuthResponse registerGameServer(GameChannelHandler gameChannelHandler, byte gameServerId,
		byte[] defaultAddress, String password)
	{
		GAMESERVER_ID = gameServerId;
		return passwordConfigAuth(password);
	}

	/**
	 * 
	 * @return
	 */
	private static GsAuthResponse passwordConfigAuth(String password)
	{
		if (password.equals(Config.GAME_SERVER_PASSWORD))
			return GsAuthResponse.AUTHED;

		return GsAuthResponse.NOT_AUTHED;
	}

}
