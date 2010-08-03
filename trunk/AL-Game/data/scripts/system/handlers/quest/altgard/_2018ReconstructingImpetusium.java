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

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Mr. Poke
 *
 */
public class _2018ReconstructingImpetusium extends QuestHandler
{

	private final static int	questId	= 2018;

	public _2018ReconstructingImpetusium()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.addQuestLvlUp(questId);
		qe.setNpcQuestData(203649).addOnTalkEvent(questId);
		qe.setNpcQuestData(210588).addOnKillEvent(questId);
		qe.setNpcQuestData(700097).addOnTalkEvent(questId);
		qe.setNpcQuestData(700098).addOnTalkEvent(questId);
		qe.setNpcQuestData(210752).addOnKillEvent(questId);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		boolean lvlCheck = QuestService.checkLevelRequirement(questId, player.getCommonData().getLevel());
		if(qs == null || qs.getStatus() != QuestStatus.LOCKED || !lvlCheck)
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

		final int var = qs.getQuestVarById(0);
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(qs.getStatus() == QuestStatus.START)
		{
			switch (targetId)
			{
				case 203649:
					switch(env.getDialogId())
					{
						case 25:
							if(var == 0)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
							else if (var == 4)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
							else if (var == 7)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
							break;
						case 10000:
						case 10001:
							if (var == 0 || var == 4)
							{
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
						case 33:
							if (var == 7)
							{
								if(QuestService.collectItemCheck(env, true))
								{
									qs.setStatus(QuestStatus.REWARD);
									updateQuestStatus(player, qs);
									return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
								}
								else
									return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2120);
							}
					}
					break;
				case 700097:
					if (var == 5)
						return true;
					break;
				case 700098:
					switch(env.getDialogId())
					{
						case -1:
							env.setQuestId(questId);
							if(var == 5 && QuestService.collectItemCheck(env, false))
							{
								final int targetObjectId = env.getVisibleObject().getObjectId();
								PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000,
									1));
								PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.NEUTRALMODE2, 0,
									targetObjectId), true);
								ThreadPoolManager.getInstance().schedule(new Runnable(){
									@Override
									public void run()
									{
										Npc npc = (Npc)player.getTarget();
										if(!player.isTargeting(targetObjectId))
											return;
										PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(),
											targetObjectId, 3000, 0));
										PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_LOOT, 0,
											targetObjectId), true);

										QuestService.addNewSpawn(player.getWorldId(), player.getInstanceId(), 210752, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true);
										npc.getController().onDie(null);
										
									}
								}, 3000);
								return true;
							}
					}
					break;
			}
		}
		else if(qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 203649)
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


		int targetId = 0;
		int var = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		env.setQuestId(questId);
		switch(targetId)
		{
			case 210588:
				var = qs.getQuestVarById(0);
				if (var < 4)
				{
					qs.setQuestVarById(0, var+1);
					updateQuestStatus(player, qs);
				}
				break;
			case 210752:
				var = qs.getQuestVarById(0);
				if (var == 5 && QuestService.collectItemCheck(env, false))
				{
					qs.setQuestVarById(0, 7);
					updateQuestStatus(player, qs);
				}
				break;
		}
		return false;
	}
}
