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
package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.itemengine.actions.EnchantItemAction;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer
 */
public class CM_MANASTONE extends AionClientPacket
{
	
	private int npcObjId;
	private int slotNum;
	
	private int actionType;
	private int stoneUniqueId;
	private int targetItemUniqueId;
	private int supplementUniqueId;
	
	/**
	 * @param opcode
	 */
	public CM_MANASTONE(int opcode)
	{
		super(opcode);
	}

	@Override
	protected void readImpl()
	{
		actionType = readC();
		readC();
		targetItemUniqueId = readD();
		switch(actionType)
		{
			case 1:
			case 2:
				stoneUniqueId = readD();
				supplementUniqueId = readD();
				break;
			case 3:
				slotNum = readC();
				readC();
				readH();
				npcObjId = readD();
				break;
		}
	}

	@Override
	protected void runImpl()
	{
		AionObject npc = World.getInstance().findAionObject(npcObjId);
		Player player = getConnection().getActivePlayer();
		
		switch(actionType)
		{
			case 1: //enchant stone
			case 2: //add manastone
				EnchantItemAction action = new EnchantItemAction();
				Item manastone = player.getInventory().getItemByObjId(stoneUniqueId);
				Item targetItem = player.getInventory().getItemByObjId(targetItemUniqueId);
				if(targetItem == null)
				{
					targetItem = player.getEquipment().getEquippedItemByObjId(targetItemUniqueId);
				}
				if(manastone != null && targetItem != null && action.canAct(player, manastone, targetItem))
				{
					Item supplement = player.getInventory().getItemByObjId(supplementUniqueId);
					action.act(player, manastone, targetItem, supplement);
				}
				break;
			case 3: // remove manastone
				long price = player.getPrices().getPriceForService(500);
				if (!ItemService.decreaseKinah(player, price))
				{
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.NOT_ENOUGH_KINAH(price));
					return;
				}
				if(npc != null)
				{
					player.getInventory().decreaseKinah(price);
					ItemService.removeManastone(player, targetItemUniqueId, slotNum);
				}
		}
	}
}
