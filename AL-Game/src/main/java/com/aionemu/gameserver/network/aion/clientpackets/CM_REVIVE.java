/*
 * This file is part of aion-unique <aion-unique.com>.
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

import com.aionemu.gameserver.controllers.ReviveType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;

/**
 * @author ATracer, orz, avol, Simple
 *
 */
public class CM_REVIVE extends AionClientPacket
{
	private int reviveId;
	
	/**
	 * Constructs new instance of <tt>CM_REVIVE </tt> packet
	 * @param opcode
	 */
	public CM_REVIVE(int opcode)
	{
		super(opcode);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl()
	{
		reviveId = readC();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl()
	{
		Player activePlayer = getConnection().getActivePlayer();
		
		ReviveType reviveType = ReviveType.getReviveTypeById(reviveId);
		
		switch(reviveType)
		{
			case BIND_REVIVE:
				activePlayer.getReviveController().bindRevive();
				break;
			case REBIRTH_REVIVE:
				activePlayer.getReviveController().rebirthRevive();
				break;
			case ITEM_SELF_REVIVE:
				activePlayer.getReviveController().itemSelfRevive();
				break;
			case SKILL_REVIVE:
				activePlayer.getReviveController().skillRevive();
				break;
			case KISK_REVIVE:
				activePlayer.getReviveController().kiskRevive();
				break;
			default:
				break;
		}
		
	}
}
