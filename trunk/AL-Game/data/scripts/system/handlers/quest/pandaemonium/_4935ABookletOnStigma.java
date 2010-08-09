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
package quest.pandaemonium;

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
 * @author kecimis
 * 
 */
public class _4935ABookletOnStigma extends QuestHandler
{
	
	private final static int	questId	= 4935;
	private final static int[]	npc_ids	= { 204051, 204285, 279005 };
  /*
   * 204051 - Vergelmir
   * 204285 - Teirunerk
   * 279005 - Kohrunerk
   * 
   * 182207104 - Pirates Research Log               
   */         

  public _4935ABookletOnStigma()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(204051).addOnQuestStart(questId);	//Vergelmir
    qe.setQuestItemIds(182207107).add(questId); //Teirunerks Letter
    qe.setQuestItemIds(182207108).add(questId);	//Tattered Booklet	
		for(int npc_id : npc_ids)
			qe.setNpcQuestData(npc_id).addOnTalkEvent(questId);	 
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
			if(targetId == 204051)//Vergelmir
			{
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 4762);
				else return defaultQuestStartDialog(env);
        
      }  
      return false;
    }
	
    
    int var = qs.getQuestVarById(0);
    
    if(qs.getStatus() == QuestStatus.REWARD)
		{
		 
     if(targetId == 204051 && player.getInventory().getItemCountByItemId(182207108) == 1)//Vergelmir
			{
				if(env.getDialogId() == -1)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10002);
				else if(env.getDialogId() == 1009)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
		    else return defaultQuestEndDialog(env); 
     	}
		 return false;	
		}
		else if (qs.getStatus() == QuestStatus.START)
		{
     if(targetId == 204285)//Teirunerk
		{
      switch(env.getDialogId())
					{
					case 25:
					 if (var == 0)
            return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
           if (var == 1)
            return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
          case 33:
           if (var == 1)
            {
             if(QuestService.collectItemCheck(env, true))				
					   {
						  qs.setQuestVarById(0, var + 1);					
						  updateQuestStatus(player, qs);
						  ItemService.addItems(player, Collections.singletonList(new QuestItems(182207107, 1)));
						  return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10000);
					   }
					   else
						  return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10001);
            }
          case 10000:
				  if (var == 0)
				  qs.setQuestVarById(0, var + 1);
				  updateQuestStatus(player, qs);
          PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
          return true;
          }
		return false;				
    }else if(targetId == 279005 && player.getInventory().getItemCountByItemId(182207107) == 1 )//Kohrunerk
    {
      switch(env.getDialogId())
					{
					case 25:
					 if (var == 2)
					  return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
          case 10255:
				   if (var == 2)
				   player.getInventory().removeFromBagByItemId(182207107, 1);
           ItemService.addItems(player, Collections.singletonList(new QuestItems(182207108, 1)));
           PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
           qs.setStatus(QuestStatus.REWARD);
				   updateQuestStatus(player, qs);
           return true;
          }
    
    
    }
    return false;
    }
  return false;
  }
   
}
