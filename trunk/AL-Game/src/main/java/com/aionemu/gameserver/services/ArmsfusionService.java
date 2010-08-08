/*
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
package com.aionemu.gameserver.services;

import org.apache.log4j.Logger;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UPDATE_ITEM;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * This class is responsible of Armsfusion-related tasks (fusion,breaking)
 * 
 * @author zdead
 * 
 */
public class ArmsfusionService
{
	@SuppressWarnings("unused")
	private static final Logger			log			= Logger.getLogger(ArmsfusionService.class);

	public static void fusionWeapons(Player player, int firstItemUniqueId, int secondItemUniqueId, int price)
	{
		Item firstItem = player.getInventory().getItemByObjId(firstItemUniqueId);
		if(firstItem == null)
			firstItem = player.getEquipment().getEquippedItemByObjId(firstItemUniqueId);
		
		Item secondItem = player.getInventory().getItemByObjId(secondItemUniqueId);
		if(secondItem == null)
			secondItem = player.getEquipment().getEquippedItemByObjId(secondItemUniqueId);
		
		/*
		 * Need to have items in bag, and target the fusion NPC
		 */
		if(firstItem == null || secondItem == null || !(player.getTarget() instanceof Npc))
			return;
		
		
		if(player.getInventory().getKinahItem().getItemCount() < price)
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_NOT_ENOUGH_MONEY(firstItem.getNameID(), secondItem.getNameID()));
			return;
		}
		
		/*
		 * Fusioned weapons must have same type
		 */		
		if(firstItem.getItemTemplate().getWeaponType() != secondItem.getItemTemplate().getWeaponType())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_DIFFERENT_TYPE);
			return;
		}
		
		/*
		 * Second weapon must have inferior or equal lvl. in relation to first weapon
		 */
		if(secondItem.getItemTemplate().getLevel() > firstItem.getItemTemplate().getLevel())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMPOUND_ERROR_MAIN_REQUIRE_HIGHER_LEVEL);
			return;
		}
		
		// TODO: Transfer Manastones
		
		firstItem.setFusionedItem(secondItem.getItemTemplate().getTemplateId());
		
		DAOManager.getDAO(InventoryDAO.class).store(firstItem, player.getObjectId());
		
		player.getInventory().removeFromBagByObjectId(secondItemUniqueId, 1);
		
		PacketSendUtility.sendPacket(player, new SM_DELETE_ITEM(secondItemUniqueId));
		
		PacketSendUtility.sendPacket(player, new SM_UPDATE_ITEM(firstItem));
		
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMPOUND_SUCCESS(firstItem.getNameID(), secondItem.getNameID()));
		
	}
	
	public static void breakWeapons(Player player, int weaponToBreakUniqueId)
	{
		Item weaponToBreak = player.getInventory().getItemByObjId(weaponToBreakUniqueId);
		if(weaponToBreak == null)
			weaponToBreak = player.getEquipment().getEquippedItemByObjId(weaponToBreakUniqueId);
		
		if(weaponToBreak == null || (player.getTarget() instanceof Npc))
			return;

		if(!weaponToBreak.hasFusionedItem())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOUND_ERROR_NOT_AVAILABLE(weaponToBreak.getNameID()));
			return;
		}
	
		weaponToBreak.setFusionedItem(0);
		
		DAOManager.getDAO(InventoryDAO.class).store(weaponToBreak, player.getObjectId());
		
		PacketSendUtility.sendPacket(player, new SM_UPDATE_ITEM(weaponToBreak));
		
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_COMPOUNDED_ITEM_DECOMPOUND_SUCCESS(weaponToBreak.getNameID()));
		
	}
	
}
