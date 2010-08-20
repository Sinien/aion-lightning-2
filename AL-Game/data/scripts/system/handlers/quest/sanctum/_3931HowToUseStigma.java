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
public class _3931HowToUseStigma extends QuestHandler
{
	
	private final static int	questId	= 3931;
	private final static int[]	npc_ids	= { 798321, 279005, 203711 };
  /*
   * 
   * 798321 - Koruchinerk
   * 279005 - Kohrunerk
   * 203711 - Miriya   
   * 
   * 182207104 - Pirates Research Log               
   */         

  public _3931HowToUseStigma()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(203711).addOnQuestStart(questId);	//Miriya
    qe.setQuestItemIds(182206080).add(questId); //Kohrunerks Belt
    qe.setQuestItemIds(182206081).add(questId);	//Stigma Manual	
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
			if(targetId == 203711)//Miriya
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
		 
     if(targetId == 203711 && player.getInventory().getItemCountByItemId(182206081) == 1)//Miriya
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
     if(targetId == 798321)//Koruchinerk
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
						  ItemService.addItems(player, Collections.singletonList(new QuestItems(182206080, 1)));
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
    }else if(targetId == 279005 && player.getInventory().getItemCountByItemId(182206080) == 1 )//Kohrunerk
    {
      switch(env.getDialogId())
					{
					case 25:
					 if (var == 2)
					  return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
          case 10255:
				   if (var == 2)
				   ItemService.removeItemFromInventoryByItemId(player, 182206080);
           ItemService.addItems(player, Collections.singletonList(new QuestItems(182206081, 1)));
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
