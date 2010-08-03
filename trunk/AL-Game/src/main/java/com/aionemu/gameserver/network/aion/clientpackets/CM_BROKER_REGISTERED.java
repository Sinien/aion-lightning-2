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
package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.services.BrokerService;

/**
 * @author kosyak
 *
 */
public class CM_BROKER_REGISTERED extends AionClientPacket
{
	@SuppressWarnings("unused")
	private int npcId;
	
	/**
	 * 
	 */
	public CM_BROKER_REGISTERED(int opcode)
	{
		super(opcode);
	}
	
	@Override
	protected void readImpl()
	{
		npcId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player player = getConnection().getActivePlayer();
		
		BrokerService.getInstance().showRegisteredItems(player);
	}
}
