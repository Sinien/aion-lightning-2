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

package com.aionemu.loginserver.network.gameserver.serverpackets;

import java.nio.ByteBuffer;

import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.GsServerPacket;

/**
 * In this packet LoginServer is answering on GameServer ban request
 * 
 * @author Watson
 * 
 */
public class SM_BAN_RESPONSE extends GsServerPacket
{
	private final byte		type;
	private final int		accountId;
	private final String	ip;
	private final int		time;
	private final int		adminObjId;
	private final boolean	result;

	public SM_BAN_RESPONSE(byte type, int accountId, String ip, int time, int adminObjId, boolean result)
	{
		super(0x05);

		this.type = type;
		this.accountId = accountId;
		this.ip = ip;
		this.time = time;
		this.adminObjId = adminObjId;
		this.result = result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(GsConnection con, ByteBuffer buf)
	{
		writeC(buf, getOpcode());

		writeC(buf, type);
		writeD(buf, accountId);
		writeS(buf, ip);
		writeD(buf, time);
		writeD(buf, adminObjId);
		writeC(buf, result ? 1 : 0);
	}
}        
