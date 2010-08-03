/* Created by St0rm.net
 * 
 * This file is part of aion-emu <aion-emu.com>.
 *
 *  aion-emu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-emu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-emu.  If not, see <http://www.gnu.org/licenses/>.
 */
package admincommands;

import java.util.Iterator;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Ben, Ritsu
 * 
 * Smart Matching Enabled
 * //announce anon This will work. as well as //announce a This will work.
 * Both will match the "a" or "anon" to the "anonymous" flag.
 * 
 */
public class Announce extends AdminCommand
{
	public Announce()
	{
		super("announce");
	}
	
	@Override
	public int getSplitSize()
	{
		return 2;
	}
	
	@Override
	public void executeCommand(Player admin, String[] params)
	{
		
		if(admin.getAccessLevel() < AdminConfig.COMMAND_ANNOUNCE)
		{
			PacketSendUtility.sendMessage(admin, "You dont have enough rights to execute this command.");
			return;
		}

		if(params == null || params.length != 2)
		{
			PacketSendUtility.sendMessage(admin, "Syntax: //announce <anonymous|name> <message>");
			return;
		}
		
		String message;
		
		if (("anonymous").startsWith(params[0].toLowerCase()))
		{
			message = "Announce: ";
		}
		else if (("name").startsWith(params[0].toLowerCase()))
		{
			message = admin.getName() + ": ";
		}
		else
		{
			PacketSendUtility.sendMessage(admin, "Syntax: //announce <anonymous|name> <message>");
			return;
		}
		message += params[1];
		
		Iterator<Player> iter = World.getInstance().getPlayersIterator();
		
		while(iter.hasNext())
		{
			PacketSendUtility.sendSysMessage(iter.next(), message);
		}
	}
}
