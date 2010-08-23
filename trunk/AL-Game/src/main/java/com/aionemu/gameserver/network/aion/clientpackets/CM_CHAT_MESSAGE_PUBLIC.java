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
package com.aionemu.gameserver.network.aion.clientpackets;

import javolution.util.FastList;

import org.apache.log4j.Logger;

import com.aionemu.commons.objects.filter.ObjectFilter;
import com.aionemu.gameserver.configs.main.OptionsConfig;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.alliance.PlayerAllianceGroup;
import com.aionemu.gameserver.model.alliance.PlayerAllianceMember;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.restrictions.RestrictionsManager;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ChatHandler;
import com.aionemu.gameserver.utils.chathandlers.ChatHandlerResponse;
import com.aionemu.gameserver.utils.chathandlers.ChatHandlers;

/**
 * Packet that reads normal chat messages.<br>
 * 
 * @author SoulKeeper
 */
public class CM_CHAT_MESSAGE_PUBLIC extends AionClientPacket
{
	private static final Logger	log	= Logger.getLogger(CM_CHAT_MESSAGE_PUBLIC.class);

	/**
	 * Chat type
	 */
	private ChatType			type;

	/**
	 * Chat message
	 */
	private String				message;

	/**
	 * Constructs new client packet instance.
	 * 
	 * @param opcode
	 */
	public CM_CHAT_MESSAGE_PUBLIC(int opcode)
	{
		super(opcode);
	}

	/**
	 * Reads chat message
	 */
	@Override
	protected void readImpl()
	{
		type = ChatType.getChatTypeByInt(readC());
		message = readS();
	}

	/**
	 * Prints debug info
	 */
	@Override
	protected void runImpl()
	{

		final Player player = getConnection().getActivePlayer();

		if (player == null)
			return;
		
		FastList<ChatHandler> chatHandlers = ChatHandlers.getInstance().getHandlers();
		for (FastList.Node<ChatHandler> n = chatHandlers.head(), end = chatHandlers.tail(); (n = n.getNext()) != end;)
		{
			ChatHandlerResponse response = n.getValue().handleChatMessage(type, message, player);
			if(response.isBlocked())
				return;

			message = response.getMessage();
		}

		if(RestrictionsManager.canChat(player))
		{
			switch(this.type)
			{
				case GROUP:
					if (player.getPlayerGroup() == null && player.getPlayerAlliance() == null)
						return;
					
					if(OptionsConfig.LOG_CHAT)
						log.info(String.format("[MESSAGE] - G <%d>: [%s]> %s", player.getPlayerGroup().getGroupId(), player.getName(), message));
					
					broadcastToGroupMembers(player);
					break;
				case ALLIANCE:
					if (player.getPlayerAlliance() == null)
						return;
					
					if(OptionsConfig.LOG_CHAT)
						log.info(String.format("[MESSAGE] - A <%d>: [%s]> %s", player.getPlayerAlliance().getObjectId(), player.getName(), message));
					
					broadcastToAllianceMembers(player);
					break;
					
				case GROUP_LEADER:
					if (!player.isInGroup() && !player.isInAlliance())
						return;
					
					if(OptionsConfig.LOG_CHAT)
						log.info(String.format("[MESSAGE] - LA: [%s]> %s", player.getName(), message));
					
					// Alert must go to entire group or alliance.
					if (player.isInGroup())
						broadcastToGroupMembers(player);
					else
						broadcastToAllianceMembers(player);
					break;
				case LEGION:				
					if(OptionsConfig.LOG_CHAT)
						log.info(String.format("[MESSAGE] - L <%s>: [%s]> %s", player.getLegion().getLegionName(), player.getName(), message));
					
					broadcastToLegionMembers(player);
					break;
				default:
					if(OptionsConfig.LOG_CHAT)
						log.info(String.format("[MESSAGE] - ALL: [%s]> %s", player.getName(), message));
					
					broadcastToNonBlockedPlayers(player);
				break;
			}
		}
	}

	/**
	 * Sends message to all players that are not in blocklist
	 * 
	 * @param player
	 */
	private void broadcastToNonBlockedPlayers(final Player player)
	{
		PacketSendUtility.broadcastPacket(player, new SM_MESSAGE(player, message, type), true,
			new ObjectFilter<Player>(){

				@Override
				public boolean acceptObject(Player object)
				{
					return !object.getBlockList().contains(player.getObjectId());
				}
			});
	}

	/**
	 * Sends message to all group members (regular player group, or alliance sub-group
	 * Error 105, random value for players to report. Should never happen.
	 * 
	 * @param player
	 */
	private void broadcastToGroupMembers(final Player player)
	{
		if(player.isInGroup())
		{
			for(Player groupPlayer : player.getPlayerGroup().getMembers())
			{
				PacketSendUtility.sendPacket(groupPlayer, new SM_MESSAGE(player, message, type));
			}
		}
		else if (player.isInAlliance())
		{
			PlayerAllianceGroup allianceGroup = player.getPlayerAlliance().getPlayerAllianceGroupForMember(player.getObjectId());
			if(allianceGroup != null)
			{
				for(PlayerAllianceMember allianceMember : allianceGroup.getMembers())
				{
					if (allianceMember.isOnline())
						PacketSendUtility.sendPacket(allianceMember.getPlayer(),
							new SM_MESSAGE(player, message, type));
				}
			}
		}
		else
		{
			PacketSendUtility.sendMessage(player, "You are not in an alliance or group. (Error 105)");
		}
	}

	/**
	 * Sends message to all alliance members
	 * 
	 * @param player
	 */
	private void broadcastToAllianceMembers(final Player player)
	{
		PlayerAlliance alliance = player.getPlayerAlliance();
		if(alliance != null)
		{
			for(PlayerAllianceMember allianceMember : alliance.getMembers())
			{
				if (!allianceMember.isOnline()) continue;
				PacketSendUtility.sendPacket(allianceMember.getPlayer(), new SM_MESSAGE(player, message, type));
			}
		}
	}
	
	/**
	 * Sends message to all legion members
	 * 
	 * @param player
	 */
	private void broadcastToLegionMembers(final Player player)
	{
		if(player.isLegionMember())
		{
			PacketSendUtility.broadcastPacketToLegion(player.getLegion(), new SM_MESSAGE(player, message, type));
		}
	}
}
