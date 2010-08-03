/*
 * This file is part of aion-unique <aion-unique.org>.
 *
 * aion-unique is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aion-unique is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
 */
package quest.eltnen;

import java.util.Collections;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.ZoneService;
import com.aionemu.gameserver.world.zone.ZoneName;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Xitanium
 * 
 */
public class _1039SomethingInTheWater extends QuestHandler
{	

	private final static int	questId	= 1039;
	private final static int[]	mob_ids	= { 210946, 210947 };
	
	public _1039SomethingInTheWater()
	{
		super(questId);
	}
	
	@Override
	public void register()
	{
		qe.setQuestItemIds(182201009).add(questId); //Empty Water Bottle
		qe.setNpcQuestData(203946).addOnTalkEvent(questId); //Asclepius
		qe.setNpcQuestData(203705).addOnTalkEvent(questId); //Jumentis
		qe.addQuestLvlUp(questId);
		for(int mob_id : mob_ids)
		qe.setNpcQuestData(mob_id).addOnKillEvent(questId);
	}
		
	@Override
	public boolean onLvlUpEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		boolean lvlCheck = QuestService.checkLevelRequirement(questId, player.getCommonData().getLevel());
		if(qs == null || !lvlCheck || qs.getStatus() != QuestStatus.LOCKED)
			return false;
		qs.setStatus(QuestStatus.START);
		updateQuestStatus(player, qs);
		return true;
	}	
	
	@Override
	public boolean onItemUseEvent(QuestEnv env, Item item)
	{
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();

		if(id != 182201009) //Empty Water Bottle
			return false;
		if(!ZoneService.getInstance().isInsideZone(player, ZoneName.MYSTIC_SPRING_OF_AGAIRON))
			return false;
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null)
			return false;
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 3000, 0, 0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable(){
			@Override
			public void run()
			{
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
				player.getInventory().removeFromBagByObjectId(itemObjId, 1);
				ItemService.addItems(player, Collections.singletonList(new QuestItems(182201010, 1)));
				qs.setQuestVar(2);
				updateQuestStatus(player, qs);
			}
		}, 3000);
		return false;
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env) //Need to recheck because count bug
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		switch(targetId)
		{
			case 210946:
				if(qs.getQuestVarById(1) == 2 && qs.getQuestVarById(2) == 3 && var == 4)
				{
					qs.setQuestVarById(1, qs.getQuestVarById(1)+1);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(player, qs);
					return true;
				}
				else if(qs.getQuestVarById(1) <= 2 && var == 4)
				{
					qs.setQuestVarById(1, qs.getQuestVarById(1)+1);
					updateQuestStatus(player, qs);
					return true;
				}
		}
		
		switch(targetId)
		{
			case 210947:
				if(qs.getQuestVarById(1) == 3 && qs.getQuestVarById(2) == 2 && var == 4)
				{
					qs.setQuestVarById(2, qs.getQuestVarById(2)+1);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(player, qs);
					return true;
				}
				else if(qs.getQuestVarById(2) <= 2 && var == 4)
				{
					qs.setQuestVarById(2, qs.getQuestVarById(2)+1);
					updateQuestStatus(player, qs);
					return true;
				}
		}
		return false;
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null)
		return false;
		
		if(targetId == 203946) //Asclepius
		{
			if(qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0)
			{
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
				else if(env.getDialogId() == 10000)
				{
					qs.setQuestVar(1);
					ItemService.addItems(player, Collections.singletonList(new QuestItems(182201009, 1)));
					updateQuestStatus(player, qs);
					PacketSendUtility
						.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
				else
					return defaultQuestStartDialog(env);
			}
			
			else if(qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 3)
			{
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
				else if(env.getDialogId() == 10002)
				{
					qs.setQuestVar(4);
					updateQuestStatus(player, qs);
					PacketSendUtility
						.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
				else
					return defaultQuestStartDialog(env);
			}	
			
			else if(qs.getStatus() == QuestStatus.REWARD)
				{
					return defaultQuestEndDialog(env);
				}
		}
		else if(targetId == 203705) //Jumentis
		{
			if(qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 2)
			{
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
				else if(env.getDialogId() == 10001)
				{
					qs.setQuestVar(3); 
					updateQuestStatus(player, qs);
					player.getInventory().removeFromBagByItemId(182201010, 1);
					PacketSendUtility
						.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
				else
					return defaultQuestStartDialog(env);
					
			}
		}	
		return false;
		}

}
