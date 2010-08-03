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
package com.aionemu.gameserver.network.aion.serverpackets;

import java.nio.ByteBuffer;
import java.util.Map;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public class SM_SKILL_COOLDOWN extends AionServerPacket
{

	private Map<Integer, Long> cooldowns;
	
	public SM_SKILL_COOLDOWN(Map<Integer, Long> cooldowns)
	{
		this.cooldowns = cooldowns;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con, ByteBuffer buf)
	{
		writeH(buf, cooldowns.size());
		long currentTime = System.currentTimeMillis();
		for(Map.Entry<Integer, Long> entry : cooldowns.entrySet())
		{
			writeH(buf, entry.getKey());
			int left = Math.round((entry.getValue() - currentTime) / 1000);
			writeD(buf, left > 0 ? left : 0);
		}
	}
}
