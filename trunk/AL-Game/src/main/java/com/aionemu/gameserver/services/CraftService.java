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
package com.aionemu.gameserver.services;

import org.apache.log4j.Logger;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.StaticObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.recipe.Component;
import com.aionemu.gameserver.model.templates.recipe.RecipeTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.task.CraftingTask;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author MrPoke, sphinx
 *
 */
public class CraftService 
{
	private static final Logger log = Logger.getLogger(CraftService.class);

	/**
	 * 
	 * @param player
	 * @param recipetemplate
	 * @param critical
	 */
	public static void finishCrafting(Player player, RecipeTemplate recipetemplate, boolean critical)
	{
		int productItemId = 0;
		
		if (critical && recipetemplate.getComboProduct() != null)
			productItemId = recipetemplate.getComboProduct();
		else
			productItemId = recipetemplate.getProductid();		

		if(productItemId != 0)
		{
			int xpReward = (int)((0.008*(recipetemplate.getSkillpoint()+100)*(recipetemplate.getSkillpoint()+100)+60)*player.getRates().getCraftingXPRate());
			ItemService.addItem(player, productItemId, recipetemplate.getQuantity());			

			if (player.getSkillList().addSkillXp(player, recipetemplate.getSkillid(), xpReward))
				player.getCommonData().addExp(xpReward);
			else
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.MSG_DONT_GET_PRODUCTION_EXP(new DescriptionId(recipetemplate.getSkillid())));
		}
		player.setCraftingTask(null);
	}

	/**
	 * 
	 * @param player
	 * @param targetTemplateId
	 * @param recipeId
	 * @param targetObjId
	 */
	public static void startCrafting(Player player, int targetTemplateId, int recipeId, int targetObjId)
	{		
		if (player.getCraftingTask() != null && player.getCraftingTask().isInProgress())
			return;

		RecipeTemplate recipeTemplate = DataManager.RECIPE_DATA.getRecipeTemplateById(recipeId);		

		if (recipeTemplate != null)
		{
			// check for pre-usage crafting -----------------------------------------------------
			int skillId = recipeTemplate.getSkillid();
			AionObject target = World.getInstance().findAionObject(targetObjId);
			
			//morphing dont need static object/npc to use
			if ((skillId != 40009) && (target == null || !(target instanceof StaticObject)))
			{
				log.info("[AUDIT] Player " + player.getName() + " tried to craft incorrect target.");
				return;
			}
			
			if (recipeTemplate.getDp() != null && (player.getCommonData().getDp() <recipeTemplate.getDp()))
			{
				log.info("[AUDIT] Player " + player.getName() + " modded her/his client.");
				return;
			}
			if (player.getInventory().isFull())
			{
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.COMBINE_INVENTORY_IS_FULL);
				return;
			}
			
			for (Component component : recipeTemplate.getComponent())
			{
				if (player.getInventory().getItemCountByItemId(component.getItemid()) < component.getQuantity())
				{
					log.info("[AUDIT] Player " + player.getName() + " modded her/his client.");
					return;
				}
			}									
			// ---------------------------------------------------------------------------------
			
			//craft item template --------------------------------------------------------------
			ItemTemplate critItemTemplate = null;
			ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(recipeTemplate.getProductid());
			if (itemTemplate == null)
				return;			
	
			if (recipeTemplate.getComboProduct() != null)
				critItemTemplate = DataManager.ITEM_DATA.getItemTemplate(recipeTemplate.getComboProduct());
	
			if (critItemTemplate == null)
				critItemTemplate = itemTemplate;
	
			if (recipeTemplate.getDp() != null)
				player.getCommonData().addDp(-recipeTemplate.getDp());
	
			for (Component component : recipeTemplate.getComponent())
			{
				ItemService.decreaseItemCountByItemId(player, component.getItemid(), component.getQuantity());
			}
			// ----------------------------------------------------------------------------------
			
			// start crafting
			int skillLvlDiff = player.getSkillList().getSkillLevel(skillId)-recipeTemplate.getSkillpoint();
			player.setCraftingTask(new CraftingTask(player, (StaticObject)target, recipeTemplate, itemTemplate, critItemTemplate, skillLvlDiff));			
			player.getCraftingTask().start();
		}
	}	
}
