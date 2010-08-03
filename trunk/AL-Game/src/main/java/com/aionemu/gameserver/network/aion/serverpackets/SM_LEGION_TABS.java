/*
 * This file is part of aion-unique <www.aion-unique.com>.
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
import java.util.Collection;

import com.aionemu.gameserver.model.legion.LegionHistory;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Simple
 */
public class SM_LEGION_TABS extends AionServerPacket
{
	private int									page;
	private Collection<LegionHistory>	legionHistory;

	public SM_LEGION_TABS(Collection<LegionHistory> legionHistory)
	{
		this.legionHistory = legionHistory;
		this.page = 0;
	}

	public SM_LEGION_TABS(Collection<LegionHistory> legionHistory, int page)
	{
		this.legionHistory = legionHistory;
		this.page = page;
	}

	@Override
	protected void writeImpl(AionConnection con, ByteBuffer buf)
	{
		/**
		 * If history size is less than page*8 return
		 */
		if(legionHistory.size() < (page * 8))
			return;
		
		// TODO: Formula's could use a refactor
		int hisSize = legionHistory.size() - (page * 8);

		if(page == 0 && legionHistory.size() > 8)
			hisSize = 8;
		if(page == 1 && legionHistory.size() > 16)
			hisSize = 8;
		if(page == 2 && legionHistory.size() > 24)
			hisSize = 8;

		writeD(buf, 0x12); // Unk
		writeD(buf, page); // current page
		writeD(buf, hisSize);

		int i = 0;
		for(LegionHistory history : legionHistory)
		{
			if(i >= (page * 8) && i <= (8 + (page * 8)))
			{
				writeD(buf, (int) (history.getTime().getTime() / 1000));
				writeC(buf, history.getLegionHistoryType().getHistoryId());
				writeC(buf, 0);
				if(history.getName().length() > 0)
				{
					writeS(buf, history.getName());
					int size = 134 - (history.getName().length() * 2 + 2);
					writeB(buf, new byte[size]);
				}
				else
					writeB(buf, new byte[134]);
			}
			i++;
			if(i >= (8 + (page * 8)))
				break;
		}
		writeH(buf, 0);
	}
}