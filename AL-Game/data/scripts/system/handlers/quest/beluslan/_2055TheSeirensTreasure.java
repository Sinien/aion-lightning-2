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
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
 

/**
 * @author Hellboy aion4Free
 * 
 */
public class _2055TheSeirensTreasure extends QuestHandler
{	
	private final static int	questId	= 2055;
	private final static int[]	npc_ids	= { 204768, 204743, 204808 };

	public _2055TheSeirensTreasure()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.addQuestLvlUp(questId);	
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
		QuestState qs2 = player.getQuestStateList().getQuestState(2054);
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
			if(targetId == 204768)
			{
				return defaultQuestEndDialog(env);
			}
			return false;
		}
		else if(qs.getStatus() != QuestStatus.START)
		{
			return false;
		}
		if(targetId == 204768)
		{
			switch(env.getDialogId())
			{
				case 25:
					if(var == 0)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
					if(var == 2)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);	
					if(var == 6)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 3057);							
				case 1012:
					PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 239));
						break;	
				case 10000:
					if(var == 0)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						ItemService.addItems(player, Collections.singletonList(new QuestItems(182204310, 1)));
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
				case 1009:
					if(var == 6)
					{
						player.getInventory().removeFromBagByItemId(182204321, 1);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(player, qs);
						return defaultQuestEndDialog(env);
					}	
				case 10006:
					if(var == 6)
					{
						PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 241));
						player.getInventory().removeFromBagByItemId(182204321, 1);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(player, qs);
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
					}		
			}
		}
		else if(targetId == 204743)
		{
			switch(env.getDialogId())
			{
				case 25:
					if(var == 1)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);						
				case 10001:
					if(var == 1)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						player.getInventory().removeFromBagByItemId(182204310, 1);
						ItemService.addItems(player, Collections.singletonList(new QuestItems(182204311, 1)));
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}				
			}
		}
		else if(targetId == 204808)
		{
			switch(env.getDialogId())
			{
				case 25:
					if(var == 3)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
					if(var == 4)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);	
					if(var == 5)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2716);		
				case 2035:
					PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 240));
					break;		
				case 10003:
					if(var == 3)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						player.getInventory().removeFromBagByItemId(182204311, 1);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
				case 10005:
					if(var == 5)
					{
						qs.setQuestVarById(0, var + 1);					
						updateQuestStatus(player, qs);
						ItemService.addItems(player, Collections.singletonList(new QuestItems(182204321, 1)));
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}	
				case 33:
					if(QuestService.collectItemCheck(env, true))				
					{	
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10000);
					}
					else
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10001);			
			}
		}
		return false;
	}
}
