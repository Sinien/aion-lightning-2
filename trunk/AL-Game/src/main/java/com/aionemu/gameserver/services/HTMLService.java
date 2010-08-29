/*
 * This file is part of aion-lightning <aion-lightning.com>.
 *
 *  aion-lightning is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-lightning is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-lightning.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.services;

import org.apache.log4j.Logger;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTIONNAIRE;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;

/**
 * Use this service to send raw html to the client 
 * Absolut alpha phase. Not yet tested what is allowed
 * 
 * @author lhw
 */
public class HTMLService
{
	private static final Logger	log	= Logger.getLogger(HTMLService.class);

	public static void pushSurvey(String html)
	{
		int messageId = IDFactory.getInstance().nextId();
		for(Player ply : World.getInstance().getAllPlayers())
			sendData(ply, messageId, html);
	}

	public static void pushHTML(Player player, String html)
	{
		sendData(player, IDFactory.getInstance().nextId(), html);
	}

	private static void sendData(Player player, int messageId, String html)
	{
		byte packet_count = (byte) Math.ceil(html.length() / (Short.MAX_VALUE - 8) + 1);
		if(packet_count < 256)
		{
			for(byte i = 0; i < packet_count; i++)
			{
				try
				{
					int from = i * (Short.MAX_VALUE - 8), to = (i + 1) * (Short.MAX_VALUE - 8);
					if(from < 0)
						from = 0;
					if(to > html.length())
						to = html.length();
					String sub = html.substring(from, to);
					player.getClientConnection().sendPacket(new SM_QUESTIONNAIRE(messageId, i, packet_count, sub));
				}
				catch(Exception e)
				{
					log.error(e);
				}
			}
		}
	}
}
