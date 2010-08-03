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
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.BrokerItem;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author kosyachok
 *
 */
public class SM_BROKER_REGISTERED_LIST extends AionServerPacket
{
	List<BrokerItem>registeredItems;
	
	public SM_BROKER_REGISTERED_LIST(List<BrokerItem> items)
	{
		this.registeredItems = items;
	}

	@Override
	protected void writeImpl(AionConnection con, ByteBuffer buf)
	{
		writeD(buf, 0);
		writeH(buf, registeredItems.size());
		for(BrokerItem item : registeredItems)
		{
			writeD(buf, item.getItemUniqueId());
			writeD(buf, item.getItemId());
			writeQ(buf, item.getPrice());
			writeQ(buf, item.getItem().getItemCount());
			writeQ(buf, item.getItem().getItemCount());
			Timestamp currentTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
			int daysLeft = Math.round((item.getExpireTime().getTime() - currentTime.getTime()) / 86400000);
			writeH(buf, daysLeft);
			writeC(buf, 0);
			writeD(buf, item.getItemId());
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
