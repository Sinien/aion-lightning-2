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
package quest.verteron;

import java.util.Collections;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACCEPTED;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rolandas
 * 
 */
public class _1170HeadlessStoneStatue extends QuestHandler
{
	private final static int	questId	= 1170;

	public _1170HeadlessStoneStatue()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(730000).addOnQuestStart(questId); // Headless Statue Body
		qe.setNpcQuestData(730000).addOnTalkEvent(questId);
		qe.setNpcQuestData(700033).addOnTalkEvent(questId); // Head of Stone Statue
		qe.setQuestMovieEndIds(16).add(questId);
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
			if(targetId == 730000)
			{
				env.setQuestId(questId);
				QuestService.startQuest(env, QuestStatus.START);
				sendQuestDialog(player, 0, 1011);
				return false;
			}
		}
		else if (qs.getStatus() == QuestStatus.START)
		{
			if(targetId == 700033 && env.getDialogId() == -1)
			{
				final int targetObjectId = env.getVisibleObject().getObjectId();
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.SIT, 0,
					targetObjectId), true);
				ThreadPoolManager.getInstance().schedule(new Runnable(){
					@Override
					public void run()
					{
						if(!player.isTargeting(targetObjectId))
							return;
						if (ItemService.addItems(player, Collections.singletonList(new QuestItems(182200504, 1))))
						{
							((Npc)player.getTarget()).getController().onDespawn(true);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(player, qs);
						}
					}
				}, 3000);
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 730000)
			{
				if(player.getInventory().getItemCountByItemId(182200504) >= 1)
				{
					player.getInventory().removeFromBagByItemId(182200504, 1);
					PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 16));
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId)
	{
		if(movieId != 16)
			return false;
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.REWARD)
			return false;
		int rewardExp = player.getRates().getQuestXpRate() * 8410;
		player.getCommonData().addExp(rewardExp);
		qs.setStatus(QuestStatus.COMPLETE);
		qs.setCompliteCount(1);
		updateQuestStatus(player, qs);
		PacketSendUtility.sendPacket(player, new SM_QUEST_ACCEPTED(questId, QuestStatus.COMPLETE, 2));
		return true;
	}
}
