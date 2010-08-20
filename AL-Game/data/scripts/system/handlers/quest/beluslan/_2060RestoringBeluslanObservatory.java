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
package quest.beluslan;

import java.util.Collections;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.ZoneService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author kecimis
 * 
 */
public class _2060RestoringBeluslanObservatory extends QuestHandler
{

	private final static int	questId	= 2060;
	private final static int[]	npc_ids	= { 204701, 204785, 278003, 278088, 700293 };

	/*
	 * 204701 - Hod 204785 - Gwendolin 278003 - Hisui 278088 - Glati 700293 - Aetheric Field Maintaning Device
	 */

	public _2060RestoringBeluslanObservatory()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.addQuestLvlUp(questId);
		qe.setQuestItemIds(182204318).add(questId);// Empty Glass Botle
		qe.setNpcQuestData(700290).addOnKillEvent(questId);// Field Suppressor
		for(int npc_id : npc_ids)
			qe.setNpcQuestData(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		boolean lvlCheck = QuestService.checkLevelRequirement(questId, player.getCommonData().getLevel());

		if(qs == null || !lvlCheck || qs.getStatus() != QuestStatus.LOCKED)
			return false;

		QuestState qs2 = player.getQuestStateList().getQuestState(2500);
		if(qs2 == null || qs2.getStatus() != QuestStatus.COMPLETE)
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
			if(targetId == 204701)// Hod
			{
				if(env.getDialogId() == -1)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10002);
				else if(env.getDialogId() == 1009)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
				else
					return defaultQuestEndDialog(env);
			}
			return false;
		}
		else if(qs.getStatus() != QuestStatus.START)
		{
			return false;
		}

		if(targetId == 204701)// Hod
		{
			switch(env.getDialogId())
			{
				case 25:
					if(var == 0)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);

				case 10000:
					if(var == 0)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(),
							10));
						return true;
					}

			}
		}
		else if(targetId == 204785)// Gwendolin
		{
			switch(env.getDialogId())
			{
				case 25:
					if(var == 1)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);

				case 33:
					if(QuestService.collectItemCheck(env, true) && var == 4)
					{
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
					}
					else
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2461);
				case 10001:
					if(var == 1)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(),
							10));
						return true;
					}
				case 10004:
					if(var == 4)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(),
							10));
						return true;
					}
			}
		}
		else if(targetId == 278003)// Hisui
		{
			switch(env.getDialogId())
			{
				case 25:
					if(var == 2)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
				case 10002:
					if(var == 2)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(),
							10));
						return true;
					}
			}
		}
		else if(targetId == 278088)// Glati
		{
			switch(env.getDialogId())
			{
				case 25:
					if(var == 3)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
				case 2035:
					if(var == 3)
						PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 281));
					break;
				case 10003:
					if(var == 3)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						ItemService.addItems(player, Collections.singletonList(new QuestItems(182204318, 1)));
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(),
							10));
						return true;
					}
			}
		}
		else if(targetId == 700293 && var == 8)
		{
			if(env.getDialogId() == -1)
			{
				final int targetObjectId = env.getVisibleObject().getObjectId();
				PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 1));
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.NEUTRALMODE2, 0,
					targetObjectId), true);
				ThreadPoolManager.getInstance().schedule(new Runnable(){
					@Override
					public void run()
					{
						PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId,
							3000, 0));
						PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_LOOT, 0,
							targetObjectId), true);
						PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 254));
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(player, qs);
					}
				}, 3000);

			}
		}

		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int var = qs.getQuestVarById(0);
		if(qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(targetId == 700290 && qs.getQuestVarById(0) > 4 && qs.getQuestVarById(0) < 8)
		{

			qs.setQuestVarById(0, var + 1);
			updateQuestStatus(player, qs);
		}
		return false;
	}

	@Override
	public boolean onItemUseEvent(QuestEnv env, Item item)
	{
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		if(qs == null || qs.getQuestVarById(0) != 4 || id != 182204318)
			return false;
		if(!ZoneService.getInstance().isInsideZone(player, ZoneName.LEIBO_ISLAND_400010000))
			return false;

		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id,
			3000, 0, 0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable(){
			@Override
			public void run()
			{
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId,
					id, 0, 1, 0), true);
				ItemService.removeItemFromInventoryByItemId(player, 182204318);
				ItemService.addItems(player, Collections.singletonList(new QuestItems(182204319, 1)));
				updateQuestStatus(player, qs);
			}
		}, 3000);
		return true;
	}

}
