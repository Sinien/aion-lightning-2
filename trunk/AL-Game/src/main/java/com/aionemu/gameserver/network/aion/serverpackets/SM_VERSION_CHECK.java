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
package com.aionemu.gameserver.network.aion.serverpackets;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.ChatService;

/**
 * @author -Nemesiss- CC fix modified by Novo
 */

public class SM_VERSION_CHECK extends AionServerPacket
{

	/**
	 * @param chatService
	 */
	public SM_VERSION_CHECK()
	{
	}

	/**
	 * {@inheritDoc}
	 */

	@Override
	protected void writeImpl(AionConnection con, ByteBuffer buf)
	{
		writeC(buf, 0x00);
		writeC(buf, NetworkConfig.GAMESERVER_ID);
		writeD(buf, 0x000188AD);// unk
		writeD(buf, 0x000188A6);// unk
		writeD(buf, 0x00000000);// unk
		writeD(buf, 0x00018898);// unk
		writeD(buf, 0x4C346D9D);// unk
		writeC(buf, 0x00);// unk
		writeC(buf, GSConfig.SERVER_COUNTRY_CODE);// country code;
		writeC(buf, 0x00);// unk
		writeC(buf, GSConfig.SERVER_MODE);//  Server mode : 0x80 = one race / 0x01 = free race / 0x22 = Character
		writeD(buf, (int) (System.currentTimeMillis() / 1000));
		writeH(buf, 0x015E);
		writeH(buf, 0x0A01);
		writeH(buf, 0x0A01);
		writeH(buf, 0x020A);
        writeC(buf, 0x00);
        writeC(buf, 0x01);
        writeC(buf, 0x00);
        writeC(buf, 0x00);
		writeB(buf, ChatService.getIp());
		writeH(buf, ChatService.getPort());
	}
}
