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

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke
 * 
 */
public class _1111InsomniaMedicine extends QuestHandler
{
	private final static int	questId	= 1111;

	public _1111InsomniaMedicine()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(203075).addOnQuestStart(questId);
		qe.setNpcQuestData(203075).addOnTalkEvent(questId);
		qe.setNpcQuestData(203061).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(targetId == 203075)
		{
			if(qs == null)
			{
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
				else
					return defaultQuestStartDialog(env);
			}
			else if(qs.getStatus() == QuestStatus.REWARD)
			{
				if(env.getDialogId() == -1)
				{
					if(qs.getQuestVarById(0) == 2)
					{	
						player.getInventory().removeFromBagByItemId(182200222, 1);
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
					}	
					else if(qs.getQuestVarById(0) == 3)
					{	
						player.getInventory().removeFromBagByItemId(182200221, 1);
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2716);
					}	
					return false;
				}
				else if(env.getDialogId() == 1009)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), qs.getQuestVarById(0) + 3);
				else if(env.getDialogId() == 17)
				{
					QuestService.questFinish(env, qs.getQuestVarById(0) - 2);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			}
		}
		else if(targetId == 203061)
		{
			if(env.getDialogId() == 25)
			{
				if(qs.getQuestVarById(0) == 0)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
				else if(qs.getQuestVarById(0) == 1)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1353);
				return false;
			}
			else if(env.getDialogId() == 33)
			{
				if(QuestService.collectItemCheck(env, true))
				{
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(player, qs);
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1353);
				}
				else
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
			}
			else if(env.getDialogId() == 10000)
			{
				qs.setQuestVarById(0, 2);
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(player, qs);
				ItemService.addItems(player, Collections.singletonList(new QuestItems(182200222, 1)));
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
				return true;
			}
			else if(env.getDialogId() == 10001)
			{
				qs.setQuestVarById(0, 3);
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(player, qs);
				ItemService.addItems(player, Collections.singletonList(new QuestItems(182200221, 1)));
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
				return true;
			}
		}
		return false;
	}
}
