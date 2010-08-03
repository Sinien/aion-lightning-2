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
package com.aionemu.gameserver.questEngine.handlers.template;

import javolution.util.FastMap;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.handlers.models.MonsterInfo;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author MrPoke
 * 
 */
public class MonsterHunt extends QuestHandler
{
	private final int				questId;
	private final int				startNpc;
	private final int				endNpc;
	private final FastMap<Integer, MonsterInfo>	monsterInfo;

	/**
	 * @param questId
	 */
	public MonsterHunt(int questId, int startNpc, int endNpc, FastMap<Integer, MonsterInfo> monsterInfo)
	{
		super(questId);
		this.questId = questId;
		this.startNpc = startNpc;
		if (endNpc != 0)
			this.endNpc = endNpc;
		else
			this.endNpc = startNpc;
		this.monsterInfo = monsterInfo;
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(startNpc).addOnQuestStart(questId);
		qe.setNpcQuestData(startNpc).addOnTalkEvent(questId);
		for(int monsterId : monsterInfo.keySet())
			qe.setNpcQuestData(monsterId).addOnKillEvent(questId);
		if (endNpc != startNpc)
			qe.setNpcQuestData(endNpc).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
		if(qs == null || qs.getStatus() == QuestStatus.NONE || (qs.getStatus() == QuestStatus.COMPLETE && (qs.getCompliteCount() <= template.getMaxRepeatCount())))
		{
			if(targetId == startNpc)
			{
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
				else
					return defaultQuestStartDialog(env);
			}
		}
		else if(qs.getStatus() == QuestStatus.START)
		{
			for(MonsterInfo mi : monsterInfo.values())
			{
				if(mi.getMaxKill() < qs.getQuestVarById(mi.getVarId()))
					return false;
			}
			if (targetId == endNpc)
			{
			if(env.getDialogId() == 25)
				return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
			else if(env.getDialogId() == 1009)
			{
				qs.setStatus(QuestStatus.REWARD);
				qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
				updateQuestStatus(player, qs);
				return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
			}
			else
				return defaultQuestEndDialog(env);
			}
		}
		else if(qs.getStatus() == QuestStatus.REWARD && targetId == endNpc)
		{
			return defaultQuestEndDialog(env);
		}
	return false;
	}
	

	@Override
	public boolean onKillEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null)
			return false;

		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(qs.getStatus() != QuestStatus.START)
			return false;
		MonsterInfo mi = monsterInfo.get(targetId);
		if(mi == null)
			return false;
		if(mi.getMaxKill() <= qs.getQuestVarById(mi.getVarId()))
			return false;

		qs.setQuestVarById(mi.getVarId(), qs.getQuestVarById(mi.getVarId()) + 1);
		updateQuestStatus(player, qs);
		return true;
	}
}
