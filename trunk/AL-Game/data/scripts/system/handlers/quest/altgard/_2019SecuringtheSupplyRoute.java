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

import java.util.Collections;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Mr. Poke
 *
 */
public class _2019SecuringtheSupplyRoute extends QuestHandler
{

	private final static int	questId	= 2019;

	public _2019SecuringtheSupplyRoute()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.addQuestLvlUp(questId);
		qe.setNpcQuestData(798033).addOnTalkEvent(questId);
		qe.setNpcQuestData(210492).addOnKillEvent(questId);
		qe.setNpcQuestData(210493).addOnKillEvent(questId);		
		qe.setNpcQuestData(203673).addOnTalkEvent(questId);
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
				case 798033:
					switch(env.getDialogId())
					{
						case 25:
							if(var == 0)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
							else if (var == 4)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
							break;
						case 10000:
							if (var == 0)
							{
								qs.setQuestVarById(0, var+1);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
						case 10001:
							if (var== 4)
							{
								if (!ItemService.addItems(player, Collections.singletonList(new QuestItems(182203024, 1))))
									return true;
								qs.setQuestVarById(0, var+1);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
				case 203673:
					switch(env.getDialogId())
					{
						case 25:
							if(var == 5)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
						case 1009:
							if (var==5)
							{
								player.getInventory().removeFromBagByItemId(182203024, 1);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(player, qs);
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
							}
					}
					
			}
		}
		else if(qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 203673)
					return defaultQuestEndDialog(env);
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

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		
		switch(targetId)
		{
			case 210492:
			case 210493:
				if (var >= 1 && var < 4)
				{
					qs.setQuestVarById(0, var+1);
					updateQuestStatus(player, qs);
					return true;
				}
		}
		return false;
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

}
