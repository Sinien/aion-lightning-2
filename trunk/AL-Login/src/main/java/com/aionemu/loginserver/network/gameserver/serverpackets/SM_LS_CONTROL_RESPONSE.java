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
 * 
 * @author Aionchs-Wylovech
 * 
 */
public class SM_LS_CONTROL_RESPONSE extends GsServerPacket
{

	private int	type;

	private boolean	result;

	private String	playerName;

	private int	param;

	private String	adminName;

	private int	accountId;


	public SM_LS_CONTROL_RESPONSE(int type, boolean result, String playerName, int accountId, int param, String adminName)
	{
		super(0x04);

		this.type = type;
		this.result = result;
		this.playerName = playerName;
		this.param = param;
		this.adminName = adminName;
		this.accountId = accountId;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(GsConnection con, ByteBuffer buf)
	{
		writeC(buf, getOpcode());

		writeC(buf, type);
		writeC(buf, result ? 1 : 0);
		writeS(buf, adminName);
		writeS(buf, playerName);
		writeC(buf, param);
		writeD(buf, accountId);
	}
}        
