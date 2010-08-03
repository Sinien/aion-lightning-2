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

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
* @author Atomics
*/
public class _1346KillingforCastor extends QuestHandler
{
	
	private final static int	questId	= 1346;

	public _1346KillingforCastor()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(203966).addOnQuestStart(questId);
		qe.setNpcQuestData(210844).addOnKillEvent(questId);
		qe.setNpcQuestData(210878).addOnKillEvent(questId);
		qe.setNpcQuestData(203966).addOnTalkEvent(questId);
		qe.setNpcQuestData(203965).addOnTalkEvent(questId);
	}

	@Override
	public boolean onKillEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		int var = qs.getQuestVarById(0);
		int var1 = qs.getQuestVarById(1);
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(targetId == 210844)
		{
			if(var >= 0 && var < 15)
			{
				qs.setQuestVarById(0, var + 1);
				updateQuestStatus(player, qs);
				return true;
			}
		}
		else if(targetId == 210878)
		{
			if(var1 >= 0 && var1 < 20)
			{
				qs.setQuestVarById(1, var1 + 1);
				updateQuestStatus(player, qs);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(player.getCommonData().getLevel() < 27)
			return false;
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		
		if(qs == null || qs.getStatus() == QuestStatus.NONE)
		{
			if(targetId == 203966)
			{
				if(env.getDialogId() == 25)
				   return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
				else
				   return defaultQuestStartDialog(env);
			}
		}
		else if(qs.getStatus() == QuestStatus.START)
		{
			if(targetId == 203965)
			{
				if(env.getDialogId() == 25 && qs.getQuestVarById(1) == 20 && qs.getQuestVarById(0) == 15)
				{
				    qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(player, qs);
				    return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
				}
				else
				   return defaultQuestStartDialog(env);
			}
		}
		else if(qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 203965)
			{
				if(env.getDialogId() == 25)
				   return sendQuestDialog(player, env.getVisibleObject().getObjectId(),5);
				   else if(env.getDialogId() == 1009)
				   return sendQuestDialog(player, env.getVisibleObject().getObjectId(),5);
				else
				   return defaultQuestEndDialog(env);
			}
		}
		return false;
	}

}
