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
import com.aionemu.gameserver.services.TeleportService;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Xitanium
 *
 */
public class _1430ATeleportationExperiment extends QuestHandler 
{

	private final static int	questId	= 1430;

	public _1430ATeleportationExperiment()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(203919).addOnQuestStart(questId); //Onesimus
		qe.setNpcQuestData(203919).addOnTalkEvent(questId); //Onesimus
		qe.setNpcQuestData(203337).addOnTalkEvent(questId); //Sonirim
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() == QuestStatus.NONE)
		{
			if(targetId == 203919) //Onesimus
			{
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 4762);
				else
					return defaultQuestStartDialog(env);
			}
		}
		
		else if(targetId == 203337) //Sonirim
		{

			if(qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0)
			{
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
				else if(env.getDialogId() == 10000)
				{
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(player, qs);
					qs.setStatus(QuestStatus.REWARD);
					TeleportService.teleportTo(player, 220020000, 1, 638, 2337, 425, (byte) 20, 0);
				}
				else
					return defaultQuestStartDialog(env);
			}
			
			else if(qs != null && qs.getStatus() == QuestStatus.REWARD) //Reward
			{
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 4080);
				else if(env.getDialogId() == 1009)
				{
					qs.setQuestVar(2);
					updateQuestStatus(player, qs);
					return defaultQuestEndDialog(env);
				}
				else
					return defaultQuestEndDialog(env);
			}

		}
		return false;
	}
}
