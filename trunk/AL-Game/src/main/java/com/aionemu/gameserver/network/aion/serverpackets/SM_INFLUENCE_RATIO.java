/*
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
package com.aionemu.gameserver.network.aion.serverpackets;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.siege.Influence;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.SiegeService;

/**
 * @author Nemiroff
 * Total Influence Ratio
 */
public class SM_INFLUENCE_RATIO extends AionServerPacket
{

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con, ByteBuffer buf)
	{
		Influence inf = Influence.getInstance();
		
		writeD(buf, SiegeService.getInstance().getSiegeTime());
        writeF(buf, inf.getElyos());
        writeF(buf, inf.getAsmos());
        writeF(buf, inf.getBalaur());
        
        //TODO: 1.9 has writeH(buf, 3) with balauria values
        writeH(buf, 1);
        
        writeD(buf, 400010000);
        writeF(buf, inf.getElyos());
        writeF(buf, inf.getAsmos());
        writeF(buf, inf.getBalaur());
        
	}
}
