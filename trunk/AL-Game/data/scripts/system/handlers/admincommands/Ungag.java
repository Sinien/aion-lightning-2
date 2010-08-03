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

import java.util.concurrent.Future;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Watson
 * 
 */
public class Ungag extends AdminCommand
{
	public Ungag()
	{
		super("ungag");
	}

	@Override
	public void executeCommand(Player admin, String[] params)
	{
		if (admin.getAccessLevel() < AdminConfig.COMMAND_GAG)
		{
			PacketSendUtility.sendMessage(admin, "You dont have enough rights to execute this command!");
			return;
		}
		
		if (params == null || params.length < 1)
		{
			PacketSendUtility.sendMessage(admin, "Syntax: //ungag <player>");
			return;
		}
		
		String name = Util.convertName(params[0]);
		Player player = World.getInstance().findPlayer(name);
		if (player == null)
		{
			PacketSendUtility.sendMessage(admin, "Player " + name + " was not found!");
			PacketSendUtility.sendMessage(admin, "Syntax: //ungag <player>");
			return;
		}
		
		player.setGagged(false);
		Future<?> task = player.getController().getTask(TaskId.GAG);
		if(task != null)
			player.getController().cancelTask(TaskId.GAG);
		PacketSendUtility.sendMessage(player, "You have been ungagged");
		
		PacketSendUtility.sendMessage(admin, "Player " + name + " ungagged");
	}
}
