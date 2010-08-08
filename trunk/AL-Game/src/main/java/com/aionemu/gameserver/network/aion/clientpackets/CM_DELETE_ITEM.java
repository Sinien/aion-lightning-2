/**
 * This file is part of aion-unique <aion-unique.smfnew.com>.
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
package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Storage;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
/**
 * 
 * @author Avol
 * 
 */
public class CM_DELETE_ITEM extends AionClientPacket
{
	public int objId;

	public CM_DELETE_ITEM(int opcode)
	{
		super(opcode);
	}


	@Override
	protected void readImpl()
	{
		objId = readD();
	}

	@Override
	protected void runImpl()
	{

		Player player = getConnection().getActivePlayer();
		Storage bag = player.getInventory();
		Item resultItem = bag.getItemByObjId(objId);
		if (resultItem.getItemTemplate().isUndeletableQuestItem())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_GIVEUP_WHEN_DELETE_QUEST_ITEM_IMPOSSIBLE(new DescriptionId(Integer
				.parseInt(resultItem.getName())))); // TODO specify the quest name wich item belongs to
			return;
		}
		if (resultItem != null)
			bag.removeFromBag(resultItem, true);
		sendPacket(new SM_DELETE_ITEM(objId));
	}
}
