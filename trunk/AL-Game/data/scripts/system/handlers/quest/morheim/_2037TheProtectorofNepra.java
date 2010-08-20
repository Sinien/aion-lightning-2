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
package quest.morheim;

import java.util.Collections;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.zone.ZoneName;


/**
 * @author Hellboy aion4Free
 *
 */
public class _2037TheProtectorofNepra extends QuestHandler
{	
	private final static int	questId	= 2037;
	private final static int[]	npc_ids	= { 204369, 204361, 278004};

	public _2037TheProtectorofNepra()
	{
		super(questId);
	}
	@Override
	public void register()
	{
		qe.addQuestLvlUp(questId);
		qe.setNpcQuestData(212861).addOnKillEvent(questId);
		qe.setQuestEnterZone(ZoneName.ALTAR_OF_THE_BLACK_DRAGON_220020000).add(questId);
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
		if(qs.getStatus() == QuestStatus.START)
		{
			switch (targetId)
			{
				case 204369:
				{
					switch(env.getDialogId())
					{
						case 25:
							if(var == 0)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
						case 1012:
							PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 80));
							break;		
						case 10000:
							if (var == 0)
							{
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
				}				
				case 204361:
				{
					switch(env.getDialogId())
					{
						case 25:
							if(var ==1)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
							else if (var==3 && (player.getInventory().getItemCountByItemId(182204015) == 1))								
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
							else if (var==5)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2716);
							else if (var==7)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 3057);	
						case 10001:
							if (var == 1)
							{
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
						case 10003:
							if (var == 3)
							{					
								ItemService.removeItemFromInventoryByItemId(player, 182204015);
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}	
						case 10005:
							if (var == 5)
							{								
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}	
						case 10255:
							if (var == 7)
							{								
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}		
					}
				}
				case 278004:
				{
					switch(env.getDialogId())
					{
						case 25:
							if(var == 2)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
						case 10002:
							if (var == 2)
							{	
								ItemService.addItems(player, Collections.singletonList(new QuestItems(182204015, 1)));
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}							
					}
				}
				
			}
		}
		else if(qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 204369)
			{
				if(env.getDialogId() == -1)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10002);
				else	
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
		if(qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(qs.getStatus() != QuestStatus.START)
			return false;
		switch(targetId)
		{
			case 212861:
				if(var == 6)
				{						
					qs.setQuestVarById(0, var + 1);
					updateQuestStatus(player, qs);
					return true;
				} 			
		}
		return false;
	}
	
	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName)
	{
		if(zoneName != ZoneName.ALTAR_OF_THE_BLACK_DRAGON_220020000)
			return false;
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs.getQuestVarById(0) == 4)
		{	
			env.setQuestId(questId);
			qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
			updateQuestStatus(player, qs);
			PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 81));
			return true;
		}
		return false;
	}	
}
