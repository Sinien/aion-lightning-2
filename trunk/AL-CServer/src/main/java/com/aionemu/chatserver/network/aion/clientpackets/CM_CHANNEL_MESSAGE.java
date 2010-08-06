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
package com.aionemu.chatserver.network.aion.clientpackets;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;

import com.aionemu.chatserver.model.channel.Channel;
import com.aionemu.chatserver.model.channel.Channels;
import com.aionemu.chatserver.model.message.Message;
import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;
import com.aionemu.chatserver.service.BroadcastService;

/**
 * 
 * @author ATracer
 */
public class CM_CHANNEL_MESSAGE extends AbstractClientPacket
{
	@SuppressWarnings("unused")
	private static final Logger	log	= Logger.getLogger(CM_CHANNEL_MESSAGE.class);

	private int					channelId;
	private byte[]				content;

	private BroadcastService	broadcastService;

	/**
	 * 
	 * @param channelBuffer
	 * @param gameChannelHandler
	 * @param opCode
	 */
	public CM_CHANNEL_MESSAGE(ChannelBuffer channelBuffer, ClientChannelHandler gameChannelHandler,
		BroadcastService broadcastService)
	{
		super(channelBuffer, gameChannelHandler, 0x18);
		this.broadcastService = broadcastService;
	}

	@Override
	protected void readImpl()
	{
		readH();
		readC();
		readD();
		readD();
		channelId = readD();
		int lenght = readH() * 2;
		content = readB(lenght);
	}

	@Override
	protected void runImpl()
	{
		Channel channel = Channels.getChannelById(channelId);
		Message message = new Message(channel, content, clientChannelHandler.getChatClient());
		broadcastService.broadcastMessage(message);
	}

	@Override
	public String toString()
	{
		return "CM_CHANNEL_MESSAGE [channelId=" + channelId + ", content=" + Arrays.toString(content) + "]";
	}
}
