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

import javolution.util.FastMap;

import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.SiegeService;

/**
 * @author Sarynth - packets from rhys2002.
 *
 */

// SM_SIEGE_LOCATION_INFO
public class SM_SIEGE_LOCATION_INFO extends AionServerPacket
{
	/***
	 * infoType
	 *  0 - reset
	 *  1 - change
	 */
	private int infoType;
	
	private FastMap<Integer, SiegeLocation> locations;
	
	public SM_SIEGE_LOCATION_INFO()
	{
		this.infoType = 0; // Reset
		locations = SiegeService.getInstance().getSiegeLocations();
	}
	
	/**
	 * @param loc
	 */
	public SM_SIEGE_LOCATION_INFO(SiegeLocation loc)
	{
		this.infoType = 1; // Update
		locations = new FastMap<Integer, SiegeLocation>();
		locations.put(loc.getLocationId(), loc);
	}

	@Override
	protected void writeImpl(AionConnection con, ByteBuffer buf)
	{
		if (SiegeConfig.SIEGE_ENABLED == false)
		{
			// Siege is Disabled
			writeC(buf, 0);
			writeH(buf, 0);
			return;
		}
		
		writeC(buf, infoType);
		writeH(buf, locations.size());
		for(FastMap.Entry<Integer, SiegeLocation> e = locations.head(), end = locations.tail();
			(e = e.getNext()) != end;)
		{
			SiegeLocation sLoc = e.getValue();
			
			writeD(buf, sLoc.getLocationId()); // Artifact ID
			writeD(buf, sLoc.getLegionId()); // Legion ID
			
			writeD(buf, 0); // unk
			writeD(buf, 0); // unk
			
			writeC(buf, sLoc.getRace().getRaceId());
			
			// is vulnerable (0 - no, 2 - yes)
			writeC(buf, sLoc.isVulnerable() ? 2 : 0);
			
			 // faction can teleport (0 - no, 1 - yes)
			writeC(buf, 1);
			
			// Next State (0 - invulnerable, 1 - vulnerable)
			writeC(buf, sLoc.getNextState());
			
			writeD(buf, 0); // unk
			writeD(buf, 0); // unk 1.9 only
		}
	}
}
