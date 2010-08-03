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

import com.aionemu.gameserver.model.gameobjects.BrokerItem;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author kosyachok
 *
 */
public class SM_BROKER_REGISTRATION_SERVICE extends AionServerPacket
{
	private BrokerItem itemForRegistration;
	private int message;
	
	public SM_BROKER_REGISTRATION_SERVICE(BrokerItem item)
	{
		this.message = 0;
		this.itemForRegistration = item;
	}
	
	public SM_BROKER_REGISTRATION_SERVICE(int message)
	{
		this.message = message;
		this.itemForRegistration = null;
	}
	
	@Override
	protected void writeImpl(AionConnection con, ByteBuffer buf)
	{
		writeH(buf, message);
		if(message == 0)
		{
			writeD(buf, itemForRegistration.getItemUniqueId());
			writeD(buf, itemForRegistration.getItemId());
			writeQ(buf, itemForRegistration.getPrice());
			writeQ(buf, itemForRegistration.getItem().getItemCount());
			writeQ(buf, itemForRegistration.getItem().getItemCount());
			writeH(buf, 8); //days left
			writeC(buf, 0);
			writeD(buf, itemForRegistration.getItemId());
			writeD(buf, 0);
			writeD(buf, 0);
			writeD(buf, 0);
			writeD(buf, 0);
			writeD(buf, 0);
			writeD(buf, 0);
			writeD(buf, 0);
			writeD(buf, 0);
			writeH(buf, 0);
		}

	}
}
