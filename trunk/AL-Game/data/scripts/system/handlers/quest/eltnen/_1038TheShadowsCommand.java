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

import java.util.Collections;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rhys2002
 * 
 */
public class _1038TheShadowsCommand extends QuestHandler
{
	private final static int	questId	= 1038;// Replace dionera in npc file
	private final static int[]	npc_ids	= { 203933, 700172, 203991, 700162 };
	
	public _1038TheShadowsCommand()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.addQuestLvlUp(questId);
		qe.setNpcQuestData(204005).addOnKillEvent(questId);		
		for(int npc_id : npc_ids)
			qe.setNpcQuestData(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		boolean lvlCheck = QuestService.checkLevelRequirement(questId, player.getCommonData().getLevel());
		if(qs == null || qs.getStatus() != QuestStatus.LOCKED  || !lvlCheck)
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

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 203991)
				return defaultQuestEndDialog(env);
		}
		else if(qs.getStatus() != QuestStatus.START)
		{
			return false;
		}
		if(targetId == 700162 && var == 0)
		{
			switch(env.getDialogId())
			{
				case -1:
				{
				final int targetObjectId = env.getVisibleObject().getObjectId();
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 1));
						PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.NEUTRALMODE2, 0, targetObjectId), true);
						ThreadPoolManager.getInstance().schedule(new Runnable(){
							@Override
							public void run()
							{
								PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 0));
								PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_LOOT, 0, targetObjectId), true);
								PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 34));
								qs.setQuestVarById(0, 1);
								updateQuestStatus(player, qs);
							}
					}, 3000);
				}
				return false;					
			}
		}
		else if(targetId == 203933)
		{
			switch(env.getDialogId())
			{
				case 25:
					if(var == 1)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
					else if (var ==3)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1694);
					else if (var ==4)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
				case 33:
					if(QuestService.collectItemCheck(env, true))
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2035);
					else
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2120);		
				case 10001:
					if(var == 1)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
				case 10002:
					if(var == 3)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
				case 10003:
					if(var == 4)
					{
						qs.setQuestVarById(0, var + 2);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}						
				return false;
			}
		}
		else if(targetId == 700172 && var == 2)
		{
			switch(env.getDialogId())
			{
				case -1:
				{
				final int targetObjectId = env.getVisibleObject().getObjectId();
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 1));
						PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.NEUTRALMODE2, 0, targetObjectId), true);
						ThreadPoolManager.getInstance().schedule(new Runnable(){
							@Override
							public void run()
							{
								PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 0));
								PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_LOOT, 0, targetObjectId), true);
								ItemService.addItems(player, Collections.singletonList(new QuestItems(182201007, 1)));
								qs.setQuestVar(3);
								updateQuestStatus(player, qs);
							}
					}, 3000);
				}
				return false;
			}
		}
		else if(targetId == 203991)
		{
			switch(env.getDialogId())
			{
				case 25:
					if(var == 6)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
				case 10004:
					if(var == 6)
					{
						PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 35));
						qs.setQuestVarById(0, 7);
						updateQuestStatus(player, qs);						
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
						QuestService.addNewSpawn(210020000, 1, 204005, (float) 1768.16, (float) 924.47, (float) 422.02, (byte) 0, true);						
						return true;
					}
				return false;
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
			
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(qs.getStatus() != QuestStatus.START || qs.getQuestVarById(0) != 7)
			return false;

		if(targetId == 204005)
		{
			qs.setStatus(QuestStatus.REWARD);
			updateQuestStatus(player, qs);
			return true;
		}
		return false;
	}		
}
