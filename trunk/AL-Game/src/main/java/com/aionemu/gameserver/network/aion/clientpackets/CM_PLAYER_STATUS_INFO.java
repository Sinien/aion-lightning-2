/**
 * This file is part of aion-emu <aion-unique.org>.
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
package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.services.AllianceService;
import com.aionemu.gameserver.services.GroupService;
import com.aionemu.gameserver.world.World;

/**
 * Called when entering the world and during group management
 * 
 * @author Lyahim
 * @author ATracer
 * @author Simple
 */

public class CM_PLAYER_STATUS_INFO extends AionClientPacket
{

	/**
	 * Definitions
	 */
	private int				status;
	private int				playerObjId;

	public CM_PLAYER_STATUS_INFO(int opcode)
	{
		super(opcode);
	}

	@Override
	protected void readImpl()
	{
		status = readC();
		playerObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player myActivePlayer = getConnection().getActivePlayer();

		switch(status)
        {
			// Note: This is currently used for PlayerGroup...
			// but it also is sent when leaving the alliance.
            case 9:
                getConnection().getActivePlayer().setLookingForGroup(playerObjId == 2);
                break;
            
            //Alliance Statuses
            case 12:
            case 14:
            case 15:
            case 19:
            case 23:
            case 24:
            	AllianceService.getInstance().playerStatusInfo(myActivePlayer, status, playerObjId);
            	break;
            
            // PlayerGroup Statuses
            case 2:
            case 3:
            case 6:
                Player player = null;

                if(playerObjId == 0)
                    player = getConnection().getActivePlayer();
                else
                    player = World.getInstance().findPlayer(playerObjId);

                if(player == null || player.getPlayerGroup() == null)
                    return;

                GroupService.getInstance().playerStatusInfo(status, player);
                break;
                
        }
	}
}