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

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Xitanium
 * 
 */
public class _1031TheMandurisSecret extends QuestHandler
{

	private final static int[]	mob_ids	= { 210770, 210771, 210759, 210758 };	
	private final static int	questId	= 1031;

	public _1031TheMandurisSecret()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(203902).addOnTalkEvent(questId); //Aurelius
		qe.setNpcQuestData(203936).addOnTalkEvent(questId); //Archelaos
		qe.setNpcQuestData(700179).addOnTalkEvent(questId); //Paper glider
		qe.setNpcQuestData(204043).addOnTalkEvent(questId); //Melginie
		qe.setNpcQuestData(204030).addOnTalkEvent(questId); //Celestine
		qe.addQuestLvlUp(questId);
				for(int mob_id : mob_ids)
			qe.setNpcQuestData(mob_id).addOnKillEvent(questId);
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
			case 210770:
			case 210771:
			case 210759:
			case 210758:
				if(var >= 1 && var <= 6)
				{
					qs.setQuestVarById(0, var + 1);
					updateQuestStatus(player, qs);
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
		if(qs == null)
		return false;
		if(targetId == 203902) //Aurelius
		{
			if(qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0)
			{
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
				else if(env.getDialogId() == 10000)
					{
					qs.setQuestVar(1);
                    updateQuestStatus(player, qs);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject()
								.getObjectId(), 10));
							return true;
					}
				else
					return defaultQuestStartDialog(env);
			}
			else if(qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 7)
			{
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
				else if(env.getDialogId() == 10001)
					{
					qs.setQuestVar(8);
                    updateQuestStatus(player, qs);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject()
								.getObjectId(), 10));
							return true;
					}
				else
					return defaultQuestStartDialog(env);
			}
			else if(qs.getStatus() == QuestStatus.REWARD)
			{
				if(env.getDialogId() == -1)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 3398);
				return defaultQuestEndDialog(env);
			}
		}
		else if(targetId == 203936) //Archelaos
		{
			if(qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 8)
			{
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
				else if(env.getDialogId() == 10002)
				{
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(player, qs);
					PacketSendUtility
						.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
				else
					return defaultQuestStartDialog(env);
			}
			
		}
		
		else if(targetId == 204043) //Melginie
		{
			if(qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 10)
			{
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
				else if(env.getDialogId() == 10004)
				{
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 2);
					updateQuestStatus(player, qs);
					PacketSendUtility
						.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
				else
					return defaultQuestStartDialog(env);
			}
			
		}
		
		else if(targetId == 204030) //Celestine
		{
			if(qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 12)
			{
				// Escort is skipped; its dialog id = 2716
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 3057);
				else if(env.getDialogId() == 10006)
				{
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(player, qs);
					PacketSendUtility
						.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
				else
					return defaultQuestStartDialog(env);
			}
			
		}
		
		else if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 9)
		{
			switch(targetId)
			{
				case 700179: //paper glider
				{
					if (qs.getQuestVarById(0) == 9 && env.getDialogId() == -1)
					{
						// it should display a dialog with id = 2034
						// action id for close = 10003
						// here is only item use action
						qs.setQuestVar(10);
						updateQuestStatus(player, qs);
						final int targetObjectId = env.getVisibleObject().getObjectId();
						PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000,
							1));
						PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.NEUTRALMODE2, 0,
							targetObjectId), true);
						ThreadPoolManager.getInstance().schedule(new Runnable(){
							@Override
							public void run()
							{
								if(player.getTarget() == null || player.getTarget().getObjectId() != targetObjectId)
									return;
								PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(),
									targetObjectId, 3000, 0));
								PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_LOOT, 0,
									targetObjectId), true);
							}
						}, 3000);
					}
				}

			}
		}

		return false;

	}
}
