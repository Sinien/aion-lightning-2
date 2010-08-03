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
 * @author Mr. Poke
 *
 */
public class _2209TheScribbler extends QuestHandler
{
	private final static int	questId	= 2209;
	
	public _2209TheScribbler()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(203555).addOnQuestStart(questId);
		qe.setNpcQuestData(203555).addOnTalkEvent(questId);
		qe.setNpcQuestData(203562).addOnTalkEvent(questId);
		qe.setNpcQuestData(203592).addOnTalkEvent(questId);
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
		{
			if(targetId == 203555)
			{
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
				else
					return defaultQuestStartDialog(env);
			}
		}
		else if (qs.getStatus() == QuestStatus.START)
		{
			switch(targetId)
			{
				case 203562:
				{
					if (qs.getQuestVarById(0) == 0)
					{
						if(env.getDialogId() == 25)
							return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
						else if(env.getDialogId() == 10000)
						{
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							updateQuestStatus(player, qs);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
					}
				}
				break;
				case 203572:
				{
					if (qs.getQuestVarById(0) == 1)
					{
						if(env.getDialogId() == 25)
							return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
						else if(env.getDialogId() == 10001)
						{
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							updateQuestStatus(player, qs);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
					}
				}
				break;
				case 203592:
				{
					if (qs.getQuestVarById(0) == 2)
					{
						if(env.getDialogId() == 25)
							return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
						else if(env.getDialogId() == 10002)
						{
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							updateQuestStatus(player, qs);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
					}
				}
				break;
				case 203555:
				{
					if (qs.getQuestVarById(0) == 3)
					{
						if(env.getDialogId() == 25)
							return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
						else if(env.getDialogId() == 1009)
							{
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(player, qs);
								return defaultQuestEndDialog(env);
							}
							else
								return defaultQuestEndDialog(env);
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 203555)
				return defaultQuestEndDialog(env);
		}
		return false;
	}
}
