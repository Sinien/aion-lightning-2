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
package quest.reshanta;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Rhys2002
 * 
 */
public class _1701GovernorsDirective extends QuestHandler
{

	private final static int	questId	= 1701;

	public _1701GovernorsDirective()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(278501).addOnTalkEvent(questId);
		qe.setQuestEnterZone(ZoneName.LATIS_PLAZA_400010000).add(questId);
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
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null)
			return false;

		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		if(targetId != 278501)
			return false;
		if(qs.getStatus() == QuestStatus.START)
		{
			if(env.getDialogId() == 25)
				return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10002);
			else if(env.getDialogId() == 1009)
			{
				qs.setStatus(QuestStatus.REWARD);
				qs.setQuestVarById(0, 1);
				updateQuestStatus(player, qs);
				return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
			}
			return false;
		}
		else if(qs.getStatus() == QuestStatus.REWARD)
		{
			if(env.getDialogId() == 17)
			{
				int[] ids = {1071, 1072, 1073, 1074, 1075, 1076, 1077};
				for (int id : ids)
				{
					QuestService.startQuest(new QuestEnv(env.getVisibleObject(), env.getPlayer(), id, env.getDialogId()), QuestStatus.LOCKED);
				}
			}
			return defaultQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName)
	{
		if(zoneName != ZoneName.LATIS_PLAZA_400010000)
			return false;
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs != null)
			return false;
		env.setQuestId(questId);
		QuestService.startQuest(env, QuestStatus.START);
		return true;
	}

}
