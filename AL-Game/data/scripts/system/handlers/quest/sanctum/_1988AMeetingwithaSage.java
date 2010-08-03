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
package quest.sanctum;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rhys2002 
 * 
 */
public class _1988AMeetingwithaSage extends QuestHandler
{
	private final static int	questId	= 1988;

	public _1988AMeetingwithaSage()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(203725).addOnQuestStart(questId);
		qe.setNpcQuestData(203989).addOnQuestStart(questId);
		qe.setNpcQuestData(798018).addOnQuestStart(questId);
		qe.setNpcQuestData(203771).addOnQuestStart(questId);
		qe.setNpcQuestData(203725).addOnTalkEvent(questId);
		qe.setNpcQuestData(203771).addOnTalkEvent(questId);		
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		
		if(qs == null || qs.getStatus() == QuestStatus.NONE) 
		{
			if(targetId == 203725)
			{			
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
				else
					return defaultQuestStartDialog(env);
			}
		}
		
		if(qs == null)
			return false;
			
		int var = qs.getQuestVarById(0);		

		if(qs.getStatus() == QuestStatus.START)
		{
			if(targetId == 203989 && var == 0)
			{
				switch(env.getDialogId())
				{
					case 25:
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
					case 10000:
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			}
			else if(targetId == 798018 && var == 1)
			{
				switch(env.getDialogId())
				{
					case 25:
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
					case 10001:
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			}
			else if(targetId == 203771 && var == 2)
			{
				switch(env.getDialogId())
				{
					case 25:
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
					case 2035:	
						if(player.getInventory().getItemCountByItemId(186000039) == 1)
						{
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(player, qs);
							player.getInventory().removeFromBagByItemId(186000039, 1);							
							return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2035);
						}
						else				
							return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2120);
				}
			}
		}
		else if(qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 203771)
				return defaultQuestEndDialog(env);
		}				
		return false;
	}
}
