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
package quest.ishalgen;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Mr. Poke, modified by Hellboy
 *
 */
public class _2122AshestoAshes extends QuestHandler
{
	private final static int	questId	= 2122;

	public _2122AshestoAshes()
	{
		super(questId);
	}
	
	@Override
	public void register()
	{
		qe.setNpcQuestData(203551).addOnTalkEvent(questId);
		qe.setNpcQuestData(700148).addOnTalkEvent(questId);
		qe.setNpcQuestData(730029).addOnTalkEvent(questId);
		qe.setQuestItemIds(182203120).add(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		if(targetId == 0)
		{	
			switch(env.getDialogId())
			{
				case 1002:
					QuestService.startQuest(env, QuestStatus.START);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
					return true;
				case 1003:
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
					return true;
			}
		}
		else if (targetId == 203551)
		{
			if (qs == null)
				return false;
			else if (qs.getStatus() == QuestStatus.START)
			{
				int var = qs.getQuestVarById(0);
				switch(env.getDialogId())
				{
					case 25:
						if (var == 0)
							return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
						break;
					case 1012:
						if (var == 0)
							player.getInventory().removeFromBagByItemId(182203120, 1);
						break;	
					case 10000:						
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
				}
			}
			else if( qs != null && qs.getStatus() == QuestStatus.REWARD)
			{
				if(env.getDialogId() == -1)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
				else
					return defaultQuestEndDialog(env);
			}
		}
		else if (targetId == 700148)
		{
			if (qs != null && qs.getStatus() == QuestStatus.START)
				return true;
		}
		else if (targetId == 730029)
		{
			if (qs != null && qs.getStatus() == QuestStatus.START)
			{
				final int targetObjectId = env.getVisibleObject().getObjectId();
				switch(env.getDialogId())
				{
					case -1:				
					if (player.getInventory().getItemCountByItemId(182203133) < 3)
						{
						sendQuestDialog(player, targetObjectId, 1693);
						return false;
						}
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000,
						1));
					PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.NEUTRALMODE2, 0,
						targetObjectId), true);
					ThreadPoolManager.getInstance().schedule(new Runnable(){
						@Override
						public void run()
						{
							if(!player.isTargeting(targetObjectId))
								return;
							PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(),
								targetObjectId, 3000, 0));
							PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_LOOT, 0,
								targetObjectId), true);
								sendQuestDialog(player, targetObjectId, 1352);
						}
					}, 3000);
							return false;
					case 10001:
						player.getInventory().removeFromBagByItemId(182203133, 3);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
						return true;
				}
				
			}
		}
		return false;
	}
	
	@Override
	public boolean onItemUseEvent(QuestEnv env, Item item)
	{
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if(id != 182203120)
			return false;
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 20, 1, 0), true);
		if(qs == null || qs.getStatus() == QuestStatus.NONE)
			sendQuestDialog(player, 0, 4);
			return true;
	}
}
