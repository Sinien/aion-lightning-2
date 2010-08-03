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
package quest.heiron;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Atomics
 */
public class _1548KlawControl extends QuestHandler
{

	private final static int	questId	= 1548;

	public _1548KlawControl()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(204583).addOnQuestStart(questId); //Senea
		qe.setNpcQuestData(204583).addOnTalkEvent(questId); //Senea
		qe.setNpcQuestData(700169).addOnKillEvent(questId); //Klaw Spawner
	}

	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		int targetId = 0;
		
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(targetId == 204583)
		{
			if(qs == null || qs.getStatus() == QuestStatus.NONE)
			{
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
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
	@Override
	public boolean onKillEvent(QuestEnv env)
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
			case 700169:
				if(var >= 0 && var < 4)
				{
					qs.setQuestVarById(0, var + 1);
					updateQuestStatus(player, qs);
					return true;
				}
				else if(var == 4)
				{
					qs.setQuestVarById(0, var + 1);
					updateQuestStatus(player, qs);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(player, qs);
					return true;
				}
		}
		return false;
	}
}
