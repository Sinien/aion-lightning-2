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
package quest.eltnen;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Atomics
 *
 */
 
public class _1367MabangtahsFeast extends QuestHandler
{

	private final static int	questId	= 1367;

	public _1367MabangtahsFeast()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(204023).addOnQuestStart(questId);
		qe.setNpcQuestData(204023).addOnTalkEvent(questId);
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(targetId == 204023)
		{
			if(qs == null || qs.getStatus() == QuestStatus.NONE)
			{
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
				else
					return defaultQuestStartDialog(env);
			}
			else if(qs.getStatus() == QuestStatus.START)
			{
				long itemCount;
				long itemCount1;
				long itemCount2;
				if(env.getDialogId() == 25 && qs.getQuestVarById(0) == 0)
				{
					itemCount = player.getInventory().getItemCountByItemId(182201333); // 2
					itemCount1 = player.getInventory().getItemCountByItemId(182201332); // 5
					itemCount2 = player.getInventory().getItemCountByItemId(182201331); // 1
					if (itemCount > 1 ||itemCount1 > 5 ||itemCount2 > 0)
					{
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
					}
					else
					{
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
					}
				}
				else if(env.getDialogId() == 10000)
				{
					itemCount2 = player.getInventory().getItemCountByItemId(182201331); // 1
					if(itemCount2 > 0)
					{
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(player, qs);
						qs.setQuestVarById(0, 1);
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
					}
					else
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
				}
				else if(env.getDialogId() == 10001)
				{
					itemCount1 = player.getInventory().getItemCountByItemId(182201332); // 5
					if(itemCount1 > 4)
					{
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(player, qs);
						qs.setQuestVarById(0, 2);
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 6);
					}
					else
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
				}
				else if(env.getDialogId() == 10002)
				{
					itemCount = player.getInventory().getItemCountByItemId(182201333); // 2
					if(itemCount > 1)
					{
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(player, qs);
						qs.setQuestVarById(0, 3);
						updateQuestStatus(player, qs);
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 7);
					}
					else
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
				}
				else
					return defaultQuestStartDialog(env);
			}
			else if(qs.getStatus() == QuestStatus.REWARD)
			{
				return defaultQuestEndDialog(env);
			}
		}
		return false;
	}

}
