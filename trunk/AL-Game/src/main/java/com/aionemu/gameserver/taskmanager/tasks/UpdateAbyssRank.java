/**
 * This file is part of aion-lightning <aion-lightning.com>.
 *
 *  aion-lightning is free software: you can redistribute it and/or modify
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
 *  along with aion-lightning.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aionemu.gameserver.taskmanager.tasks;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.tasks.TaskFromDBHandler;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANK;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author Sylvain
 *
 */
public class UpdateAbyssRank extends TaskFromDBHandler
{
	/**
	 * Logger for gameserver
	 */
	private static final	Logger		log	= Logger.getLogger(UpdateAbyssRank.class);
	
	@Override
	public String getTaskName()
	{
		return "update_abyss_rank";
	}

	@Override
	public boolean isValid()
	{
		if (params.length > 0)
			return false;
		else
			return true;
	}

	@Override
	public void run()
	{
		log.info("Task[" + id + "] launched : updating abyss ranks for all online players !");
		setLastActivation();
		
		Collection<Player> players = World.getInstance().getAllPlayers();
		
		for (Player player : players)
		{
			player.getAbyssRank().doUpdate();
			PacketSendUtility.sendPacket(player, new SM_ABYSS_RANK(player.getAbyssRank()));
		}
	}

}
