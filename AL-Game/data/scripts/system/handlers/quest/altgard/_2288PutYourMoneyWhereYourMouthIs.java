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
package quest.altgard;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Atomics
 *
 */
public class _2288PutYourMoneyWhereYourMouthIs extends QuestHandler
{

	private final static int	questId	= 2288;

	public _2288PutYourMoneyWhereYourMouthIs()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(203621).addOnQuestStart(questId);
		qe.setNpcQuestData(203621).addOnTalkEvent(questId);
		qe.setNpcQuestData(210564).addOnKillEvent(questId);
		qe.setNpcQuestData(210584).addOnKillEvent(questId);
		qe.setNpcQuestData(210581).addOnKillEvent(questId);
		qe.setNpcQuestData(201047).addOnKillEvent(questId);
		qe.setNpcQuestData(210436).addOnKillEvent(questId);
		qe.setNpcQuestData(210437).addOnKillEvent(questId);
		qe.setNpcQuestData(210440).addOnKillEvent(questId);
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(qs.getStatus() != QuestStatus.START)
			return false;
		if(targetId == 210564 || targetId == 210584 || targetId == 210581 || targetId == 210436 || targetId == 201047 || targetId == 210437 || targetId == 210440)
		{
			if(var > 0 && var < 4)
			{
				qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
				updateQuestStatus(player, qs);
				return true;
			}
			else if(var == 6)
			{
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(player, qs);
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
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
		if(targetId == 203621)
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
				if(env.getDialogId() == 25 && qs.getQuestVarById(0) == 4)
				{
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(player, qs);
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
				}
				else if(env.getDialogId() == 10000)
				{
					qs.setQuestVarById(0, 1);
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 0);
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
