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
package quest.heiron;

import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Balthazar
 */

public class _1647DressingUpForBollvig extends QuestHandler
{
	private final static int	questId	= 1647;

	public _1647DressingUpForBollvig()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(790019).addOnQuestStart(questId);
		qe.setNpcQuestData(790019).addOnTalkEvent(questId);
		qe.setQuestItemIds(182201783).add(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(qs == null || qs.getStatus() == QuestStatus.NONE)
		{
			if(targetId == 790019)
			{
				switch(env.getDialogId())
				{
					case 25:
					{
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 4762);
					}
					case 1002:
					{
						if(player.getInventory().getItemCountByItemId(182201783) == 0)
						{
							if(!ItemService.addItems(player, Collections.singletonList(new QuestItems(182201783, 1))))
							{
								return true;
							}
						}
					}
					default:
						return defaultQuestStartDialog(env);
				}
			}
		}

		if(qs == null)
			return false;

		if(qs.getStatus() == QuestStatus.REWARD)
		{
			switch(targetId)
			{
				case 790019:
				{
					switch(env.getDialogId())
					{
						case 25:
						{
							return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10002);
						}
						case 1009:
						{
							return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
						}
						default:
							return defaultQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onItemUseEvent(QuestEnv env, final Item item)
	{
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();

		if(id != 182201783)
		{
			return false;
		}

		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null)
		{
			return false;
		}

		int var = qs.getQuestVars().getQuestVars();
		if(var != 0)
		{
			return false;
		}

		if(qs.getStatus() != QuestStatus.START)
		{
			return false;
		}

		if(MathUtil.getDistance(1677, 2520, 100, player.getPosition().getX(), player.getPosition().getY(), player
			.getPosition().getZ()) > 5)
		{
			return false;
		}

		int itemId1 = 110100150;
		int itemId2 = 113100144;
		boolean CheckitemId1 = false;
		boolean CheckitemId2 = false;

		List<Item> items1 = player.getEquipment().getEquippedItemsByItemId(itemId1);
		for(@SuppressWarnings("unused")
		Item ListeCheckitemId1 : items1)
		{
			CheckitemId1 = true;
		}

		List<Item> items2 = player.getEquipment().getEquippedItemsByItemId(itemId2);
		for(@SuppressWarnings("unused")
		Item ListeCheckitemId2 : items2)
		{
			CheckitemId2 = true;
		}

		if(!CheckitemId1 && CheckitemId2 || CheckitemId1 && !CheckitemId2 || !CheckitemId1 && !CheckitemId2)
		{
			return false;
		}

		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id,
			3000, 0, 0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable(){
			@Override
			public void run()
			{
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId,
					id, 0, 1, 0), true);
				ItemService.removeItemFromInventory(player, item);
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(player, qs);
			}
		}, 3000);
		return true;
	}
}