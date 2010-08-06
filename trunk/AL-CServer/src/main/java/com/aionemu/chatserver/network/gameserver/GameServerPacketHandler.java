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
package com.aionemu.chatserver.network.gameserver;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;

import com.aionemu.chatserver.common.netty.AbstractPacketHandler;
import com.aionemu.chatserver.network.gameserver.clientpackets.CM_CS_AUTH;
import com.aionemu.chatserver.network.gameserver.clientpackets.CM_PLAYER_AUTH;
import com.aionemu.chatserver.network.gameserver.clientpackets.CM_PLAYER_LOGOUT;
import com.aionemu.chatserver.network.netty.handler.GameChannelHandler;
import com.aionemu.chatserver.network.netty.handler.GameChannelHandler.State;
import com.aionemu.chatserver.service.ChatService;
import com.aionemu.chatserver.service.GameServerService;
import com.google.inject.Inject;

/**
 * @author ATracer
 */
public class GameServerPacketHandler extends AbstractPacketHandler
{
	@SuppressWarnings("unused")
	private static final Logger	log	= Logger.getLogger(GameServerPacketHandler.class);

	@Inject
	private GameServerService	gameServerService;
	@Inject
	private ChatService			chatService;

	/**
	 * Reads one packet from ChannelBuffer
	 * 
	 * @param buf
	 * @param channelHandler
	 * @return AbstractGameClientPacket
	 */
	public AbstractGameClientPacket handle(ChannelBuffer buf, GameChannelHandler channelHandler)
	{
		byte opCode = buf.readByte();
		State state = channelHandler.getState();
		AbstractGameClientPacket gamePacket = null;

		switch (state)
		{
			case CONNECTED:
			{
				switch (opCode)
				{
					case 0x00:
						gamePacket = new CM_CS_AUTH(buf, channelHandler, gameServerService);
						break;
					default:
						unknownPacket(opCode, state.toString());
				}
				break;
			}
			case AUTHED:
			{
				switch (opCode)
				{
					case 0x01:
						gamePacket = new CM_PLAYER_AUTH(buf, channelHandler, chatService);
						break;
					case 0x02:
						gamePacket = new CM_PLAYER_LOGOUT(buf, channelHandler, chatService);
						break;
					default:
						unknownPacket(opCode, state.toString());
				}
				break;
			}
		}

		return gamePacket;
	}

}
