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
package admincommands;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemId;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Sarynth
 *
 * Simple admin assistance command for adding kinah to self, named player or target player.
 * 
 * Based on //add command.
 * Kinah Item Id - 182400001 (Using ItemId.KINAH.value())
 * 
 */
public class Kinah extends AdminCommand
{
	public Kinah()
	{
		super("kinah");
	}
	
	@Override
	public void executeCommand(Player admin, String[] params)
	{
		if(admin.getAccessLevel() < AdminConfig.COMMAND_KINAH)
		{
			PacketSendUtility.sendMessage(admin, "You dont have enough rights to execute this command.");
			return;
		}

		if(params == null || params.length < 1 || params.length > 2)
		{
			PacketSendUtility.sendMessage(admin, "syntax //kinah [player] <quantity>");
			return;
		}

		long kinahCount;
		Player receiver;
		
		if (params.length == 1)
		{
			receiver = admin;
			try
			{
				kinahCount = Integer.parseInt(params[0]);
			}
			catch (NumberFormatException e)
			{
				PacketSendUtility.sendMessage(admin, "Kinah value must be an integer.");
				return;
			}
		}
		else
		{
			receiver = World.getInstance().findPlayer(Util.convertName(params[0]));
			
			if (receiver == null)
			{
				PacketSendUtility.sendMessage(admin, "Could not find a player by that name.");
				return;
			}
			
			try
			{
				kinahCount = Integer.parseInt(params[1]);
			}
			catch (NumberFormatException e)
			{
				PacketSendUtility.sendMessage(admin, "Kinah value must be an integer.");
				return;
			}
		}
		
		long count = ItemService.addItem(receiver, ItemId.KINAH.value(), kinahCount);

		if(count == 0)
		{
			PacketSendUtility.sendMessage(admin, "Kinah given successfully.");
			PacketSendUtility.sendMessage(receiver, "An admin gives you some kinah.");
		}
		else
		{
			PacketSendUtility.sendMessage(admin, "Kinah couldn't be given.");
		}
	}
}
