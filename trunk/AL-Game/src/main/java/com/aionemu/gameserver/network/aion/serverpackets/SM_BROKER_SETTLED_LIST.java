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
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.BrokerItem;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author kosyachok
 * @author Sarynth
 */
public class SM_BROKER_SETTLED_LIST extends AionServerPacket
{
	List<BrokerItem> settledItems;
	private long totalKinah;
	
	private boolean isIconUpdate;
	private int haveItemsIcon;
	
	/**
	 * Sending settled items list
	 * @param settledItems
	 * @param totalKinah
	 */
	public SM_BROKER_SETTLED_LIST(List<BrokerItem> settledItems, long totalKinah)
	{
		this.isIconUpdate = false;
		this.settledItems = settledItems;
		this.totalKinah = totalKinah;
	}
	
	/**
	 * Send itemsToSettle icon update. 
	 * @param haveItems
	 */
	public SM_BROKER_SETTLED_LIST(boolean haveItems)
	{
		this.isIconUpdate = true;
		if (haveItems)
			this.haveItemsIcon = 1;
		else
			this.haveItemsIcon = 0;
	}
	
	@Override
	protected void writeImpl(AionConnection con, ByteBuffer buf)
	{
		if(this.isIconUpdate)
		{
			writeD(buf, 0);
			writeD(buf, this.haveItemsIcon);
			writeH(buf, 1); // unk
			writeD(buf, 0); // unk - pages?
			writeC(buf, 1); // isIconUpdate
			writeH(buf, 0); // 0 items size...
		}
		else
		{
			writeQ(buf, totalKinah);
	        
			writeH(buf, 1); // unk
	        writeD(buf, 0); // TODO: Pages
	        writeC(buf, 0); // isIconUpdate
	        
			writeH(buf, settledItems.size());
			for(int i = 0; i < settledItems.size(); i++)
			{
				writeD(buf, settledItems.get(i).getItemId());
				if(settledItems.get(i).isSold())
					writeQ(buf, settledItems.get(i).getPrice());
				else
					writeQ(buf, 0);
				writeQ(buf, settledItems.get(i).getItemCount());
				writeQ(buf, settledItems.get(i).getItemCount());
				writeD(buf, (int)settledItems.get(i).getSettleTime().getTime() / 60000);
				writeH(buf, 0);
				writeD(buf, settledItems.get(i).getItemId());
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
}
