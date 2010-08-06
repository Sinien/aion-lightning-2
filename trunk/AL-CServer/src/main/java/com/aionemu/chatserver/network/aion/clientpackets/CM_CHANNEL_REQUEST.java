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

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;

import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.model.channel.Channel;
import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.serverpackets.SM_CHANNEL_RESPONSE;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;
import com.aionemu.chatserver.service.ChatService;

/**
 * @author ATracer
 */
public class CM_CHANNEL_REQUEST extends AbstractClientPacket
{
	@SuppressWarnings("unused")
	private static final Logger	log	= Logger.getLogger(CM_CHANNEL_REQUEST.class);

	private int					channelIndex;
	private byte[]				channelIdentifier;

	/**
	 * 
	 * @param channelBuffer
	 * @param gameChannelHandler
	 * @param opCode
	 */
	public CM_CHANNEL_REQUEST(ChannelBuffer channelBuffer, ClientChannelHandler gameChannelHandler)
	{
		super(channelBuffer, gameChannelHandler, 0x10);
	}

	@Override
	protected void readImpl()
	{
		readC(); // 0x40
		readH(); // 0x00
		channelIndex = readH();
		int length = readH() * 2;
		channelIdentifier = readB(length);
	}

	@Override
	protected void runImpl()
	{
//		try
//		{
//			log.info("Channel requested " + new String(channelIdentifier, "UTF-16le"));
//		}
//		catch (UnsupportedEncodingException e)
//		{
//			e.printStackTrace();
//		}
		ChatClient chatClient = clientChannelHandler.getChatClient();
		Channel channel = ChatService.getInstance().registerPlayerWithChannel(chatClient, channelIndex, channelIdentifier);
		if (channel != null)
		{
			clientChannelHandler.sendPacket(new SM_CHANNEL_RESPONSE(chatClient, channel));
		}
	}

	@Override
	public String toString()
	{
		return "CM_CHANNEL_REQUEST [channelIndex=" + channelIndex + ", channelIdentifier="
			+ new String(channelIdentifier) + "]";
	}

}
