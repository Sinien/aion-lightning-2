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
package quest.poeta;

import java.util.Collections;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rhys2002
 * 
 */
public class _1114TheNymphsGown extends QuestHandler
{
	private final static int	questId	= 1114;
	private final static int[]	npc_ids	= { 203075, 203058, 700008 };

	public _1114TheNymphsGown()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setQuestItemIds(182200214).add(questId);
		for(int npc_id : npc_ids)
			qe.setNpcQuestData(npc_id).addOnTalkEvent(questId);	
	}

	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		int targetId = 0;
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
			
		if(targetId == 0)
		{
			if(qs == null || qs.getStatus() == QuestStatus.NONE)
			{
				if(env.getDialogId() == 1002)
				{
					QuestService.startQuest(env, QuestStatus.START);
					if(!ItemService.addItems(player, Collections.singletonList(new QuestItems(182200226, 1))));
					player.getInventory().removeFromBagByItemId(182200214, 1); // Namus's Diary with double-click to start the quest
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
					return true;
				}
				else
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
			}
		}

		if(qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		
		if(qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 203075 && var == 4) // Namus
			{
				if(env.getDialogId() == -1)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
				else if(env.getDialogId() == 1009)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 6);
				else return defaultQuestEndDialog(env);
			}
			else if(targetId == 203058 && var == 3) // Asteros
				return defaultQuestEndDialog(env);
		}
		else if(qs.getStatus() != QuestStatus.START)
			return false;

		if(targetId == 203075) // Namus
		{
			switch(env.getDialogId())
			{
				case 25:
					if(var == 0)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
					else if(var == 2)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
					else if(var == 3)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);

				case 1009:
					if(var == 2)
					{
						qs.setQuestVarById(0, var + 2);
						qs.setStatus(QuestStatus.REWARD);						
						updateQuestStatus(player, qs);
						player.getInventory().removeFromBagByItemId(182200217, 1);
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 6);
					}
					if(var == 3)
					{
						qs.setQuestVarById(0, var + 1);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(player, qs);
						player.getInventory().removeFromBagByItemId(182200217, 1);
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 6);
					}
				case 10000:
					if(var == 0)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						player.getInventory().removeFromBagByItemId(182200226, 1);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
				case 10001:
					if(var == 2)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}					
			}
		}
		else if(targetId == 700008) // Seirenia's clothes
		{
			switch(env.getDialogId())
			{
				case -1:
					if(var == 1)
					{
						final int targetObjectId = env.getVisibleObject().getObjectId();
						PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 1));
						PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.NEUTRALMODE2, 0, targetObjectId), true);
						ThreadPoolManager.getInstance().schedule(new Runnable(){
							@Override
								public void run()
							{
								PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 0));
								PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_LOOT, 0, targetObjectId), true);
								for (VisibleObject obj : player.getKnownList())
								{
									if (!(obj instanceof Npc))
										continue;
									if (((Npc)obj).getNpcId() != 203175) // Seirenia
										continue;
									((Npc)obj).getAggroList().addDamage(player, 50);
								}
								// Nymph's Dress
								if(!ItemService.addItems(player, Collections.singletonList(new QuestItems(182200217, 1))));								
								qs.setQuestVarById(0, 2);
								updateQuestStatus(player, qs);
							}
						}, 3000);
					}	
				return true;
			}
		}
		if(targetId == 203058) // Asteros
		{
			switch(env.getDialogId())
			{
				case 25:
					if(var == 3)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
				case 10002:
					if(var == 3)
					{
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(player, qs);
						player.getInventory().removeFromBagByItemId(182200217, 1);
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
					}
				case 10001:
					if(var == 3)
					{
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}					
			}
		}		
		return false;
	}
	
	@Override
	public boolean onItemUseEvent(QuestEnv env, Item item)
	{
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);		

		if(id != 182200214)
			return false;
			PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 20, 1, 0), true);
			if(qs == null || qs.getStatus() == QuestStatus.NONE)
				sendQuestDialog(player, 0, 4);
				return true;	
	}
}