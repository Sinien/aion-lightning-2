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

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
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
public class _2051SavingBeluslanFortress extends QuestHandler
{

	private final static int	questId	= 2051;
	private final static int[]	npc_ids	= { 204702, 204733, 204206, 278040, 700285 };//Artifact of Time and Space Not Appearing, so using the quest item to finish quest until its sorted!!

	public _2051SavingBeluslanFortress()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.addQuestLvlUp(questId);
		qe.setQuestItemIds(182204302).add(questId);
		for(int npc_id : npc_ids)
			qe.setNpcQuestData(npc_id).addOnTalkEvent(questId);	 
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		boolean lvlCheck = QuestService.checkLevelRequirement(questId, player.getCommonData().getLevel());
		if(qs == null || qs.getStatus() != QuestStatus.LOCKED || !lvlCheck)
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
			if(targetId == 204702)
			{
				if(env.getDialogId() == -1)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10002);
				else if(env.getDialogId() == 1009)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
				else return defaultQuestEndDialog(env);
			}
			return false;
		}
		else if(qs.getStatus() != QuestStatus.START)
		{
			return false;
		}
		if(targetId == 204702)
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
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
			}
		}
		else if(targetId == 204733)
		{
			switch(env.getDialogId())
			{
				case 25:
					if(var == 1)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
					else if(var == 2)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
					else if(var == 6)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 3057);
				case 10001:
					if(var == 1)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
				case 10002:
					if(var == 2)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
				case 10006:
					if(var == 6)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}							
			}
		}
		else if(targetId == 204206)
		{
			switch(env.getDialogId())
			{
				case 25:
					if(var == 3)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
				case 10003:
					if(var == 3)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
			}
		}
		else if(targetId == 278040)
		{
			switch(env.getDialogId())
			{
				case 25:
					if(var == 4)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
					else if(var == 5)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2716);
				case 33:
					if(QuestService.collectItemCheck(env, true))				
					{
						qs.setQuestVarById(0, var + 1);					
						updateQuestStatus(player, qs);
						ItemService.addItems(player, Collections.singletonList(new QuestItems(182204302, 1)));
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10000);
					}
					else
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10001);						
				case 10004:
					if(var == 4)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
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

		if(id != 182204302)
			return false;

		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getQuestVarById(0) != 7)
			return false;
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 3000, 0, 0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable(){
			@Override
			public void run()
			{
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
				player.getInventory().removeFromBagByItemId(182204302, 1);
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(player, qs);
			}
		}, 3000);
		return true;
	}	
}
