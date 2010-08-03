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
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RESURRECT;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Sarynth
 *
 */
public class Resurrect extends AdminCommand
{
	/**
	 * Constructor
	 */
	public Resurrect()
	{
		super("rez");
	}
	
	/**
	 * If player is still in the process of dying and this is used to resurrect
	 * with the instant flag, it may bug the player. Must wait for 2 or 3
	 * second after death before using resurrect instant. (Prompt may be used
	 * immediately.)
	 */
	@Override
	public void executeCommand(Player admin, String[] params)
	{
		if(admin.getAccessLevel() < AdminConfig.COMMAND_RESURRECT)
		{
			PacketSendUtility.sendMessage(admin, "You dont have enough rights to execute this command.");
			return;
		}
		
		final VisibleObject target = admin.getTarget();
		if(target == null)
		{
			PacketSendUtility.sendMessage(admin, "No target selected.");
			return;
		}

		if(!(target instanceof Player))
		{
			PacketSendUtility.sendMessage(admin, "You can only resurrect other players.");
			return;
		}
		
		final Player player = (Player)target;
		if (!player.getLifeStats().isAlreadyDead())
		{
			PacketSendUtility.sendMessage(admin, "That player is already alive.");
			return;
		}
		
		// Default action is to prompt for resurrect.
		if (params == null || params.length == 0 || ("prompt").startsWith(params[0]))
		{
			PacketSendUtility.sendPacket(player, new SM_RESURRECT(admin));
			return;
		}
		
		if (("instant").startsWith(params[0]))
		{
			player.getReviveController().skillRevive();
			return;
		}
		
		PacketSendUtility.sendMessage(admin, "[Resurrect] Usage: target player and use //rez <instant|prompt>");
	}

}
