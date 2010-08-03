/*
 * This file is part of aion-unique <aion-unique.com>.
 *
 * aion-unique is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aion-unique is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
 */
package admincommands;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Phantom, ATracer
 *
 */

public class Add extends AdminCommand
{
	
	public Add()
	{
		super("add");
	}

	@Override
	public void executeCommand(Player admin, String[] params)
	{
		if(admin.getAccessLevel() < AdminConfig.COMMAND_ADD)
		{
			PacketSendUtility.sendMessage(admin, "You dont have enough rights to execute this command");
			return;
		}

		if(params.length == 0 || params.length > 3)
		{
			PacketSendUtility.sendMessage(admin, "syntax //add <player> <item ID> <quantity>");
			return;
		}

		int itemId = 0;
		long itemCount = 1;
		Player receiver = null;

		try
		{
			itemId = Integer.parseInt(params[0]);
			
			if( params.length == 2 )
			{
				itemCount = Long.parseLong(params[1]);
			}
			receiver = admin;
		}
		catch (NumberFormatException e)
		{
			receiver = World.getInstance().findPlayer(Util.convertName(params[0]));
			
			if (receiver == null)
			{
				PacketSendUtility.sendMessage(admin, "Could not find a player by that name.");
				return;
			}
			
			try
			{
				itemId = Integer.parseInt(params[1]);
				
				if( params.length == 3 )
				{
					itemCount = Long.parseLong(params[2]);
				}
			}
			catch (NumberFormatException ex)
			{
			
				PacketSendUtility.sendMessage(admin, "You must give number to itemid.");
				return;
			}
			catch (Exception ex2)
			{
				PacketSendUtility.sendMessage(admin, "Occurs an error.");
				return;
			}
		}
		
		long count = ItemService.addItem(receiver, itemId, itemCount);

		if(count == 0)
		{
			PacketSendUtility.sendMessage(admin, "Item added successfully");
			PacketSendUtility.sendMessage(receiver, "Admin gives you an item");
		}
		else
		{
			PacketSendUtility.sendMessage(admin, "Item couldn't be added");
		}
		
	}
}
