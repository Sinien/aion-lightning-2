/**
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

package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUIT_RESPONSE;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.services.PlayerService;

/**
 * In this packets aion client is asking if may quit.
 * 
 * @author -Nemesiss-
 * 
 */
public class CM_QUIT extends AionClientPacket
{
	/**
	 * Logout - if true player is wanted to go to character selection.
	 */
	private boolean	logout;
	
	/**
	 * Constructs new instance of <tt>CM_QUIT </tt> packet
	 * @param opcode
	 */
	public CM_QUIT(int opcode)
	{
		super(opcode);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl()
	{
		logout = readC() == 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl()
	{
		AionConnection client = getConnection();

		if(client.getState() == State.IN_GAME)
		{
			Player player = client.getActivePlayer();
			// TODO! check if may quit
			if(!logout)
				LoginServer.getInstance().aionClientDisconnected(client.getAccount().getId());

			PlayerService.playerLoggedOut(player);

			/**
			 * clear active player.
			 */
			client.setActivePlayer(null);
		}

		if(logout)
		{
			sendPacket(new SM_QUIT_RESPONSE());
		}
		else
		{
			client.close(new SM_QUIT_RESPONSE(), true);
		}
	}
}
