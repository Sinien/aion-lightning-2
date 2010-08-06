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
package com.aionemu.chatserver.network.aion;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;

import com.aionemu.chatserver.common.netty.AbstractPacketHandler;
import com.aionemu.chatserver.network.aion.clientpackets.CM_CHANNEL_MESSAGE;
import com.aionemu.chatserver.network.aion.clientpackets.CM_CHANNEL_REQUEST;
import com.aionemu.chatserver.network.aion.clientpackets.CM_PLAYER_AUTH;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler.State;
import com.aionemu.chatserver.service.BroadcastService;
import com.aionemu.chatserver.service.ChatService;
import com.google.inject.Inject;

/**
 * @author ATracer
 */
public class ClientPacketHandler extends AbstractPacketHandler
{
	@Inject
	private BroadcastService broadcastService;
	@Inject
	private ChatService chatService;
	
	@SuppressWarnings("unused")
	private static final Logger	log	= Logger.getLogger(ClientPacketHandler.class);

	/**
	 * Reads one packet from ChannelBuffer
	 * 
	 * @param buf
	 * @param channelHandler
	 * @return AbstractClientPacket
	 */
	public AbstractClientPacket handle(ChannelBuffer buf, ClientChannelHandler channelHandler)
	{
		byte opCode = buf.readByte();
		State state = channelHandler.getState();
		AbstractClientPacket clientPacket = null;

		switch (state)
		{
			case CONNECTED:
				switch (opCode)
				{
					case 0x05:
						clientPacket = new CM_PLAYER_AUTH(buf, channelHandler, chatService);
						break;
					default:
						//unknownPacket(opCode, state.toString());
				}
				break;
			case AUTHED:
				switch (opCode)
				{
					case 0x10:
						clientPacket = new CM_CHANNEL_REQUEST(buf, channelHandler, chatService);
						break;
					case 0x18:
						clientPacket = new CM_CHANNEL_MESSAGE(buf, channelHandler, broadcastService);
					default:
						//unknownPacket(opCode, state.toString());
				}
				break;
		}

		return clientPacket;
	}
}
