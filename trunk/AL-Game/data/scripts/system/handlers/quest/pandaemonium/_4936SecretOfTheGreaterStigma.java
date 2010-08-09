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

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;


/**
 * @author kecimis
 * 
 */
public class _4936SecretOfTheGreaterStigma extends QuestHandler
{
	
	private final static int	questId	= 4936;
	private final static int[]	npc_ids	= { 204051, 204837};
  /*
   * 204051 - Vergelmir
   * 204837 - Hresvelgr
   *       
   */         

  public _4936SecretOfTheGreaterStigma()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(204051).addOnQuestStart(questId);	//Vergelmir
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
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
				else return defaultQuestStartDialog(env);
        
      }  
      return false; 
    }
	
    
    int var = qs.getQuestVarById(0);
    
    if(qs.getStatus() == QuestStatus.REWARD)
     {
      if(targetId == 204051)//Vergelmir
			{
			  return defaultQuestEndDialog(env);
      }
      return false;
     }
    else if(qs.getStatus() == QuestStatus.START)
		{
		 
     if(targetId == 204051 && var == 1)//Vergelmir
			{
			  switch(env.getDialogId())
        {
         case 25:
          return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
         case 33:
          if(QuestService.collectItemCheck(env, true))				
					   {
						  qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(player, qs);
             return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
            }
					else
						  return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2716);
         }
       
      }	
      else if(targetId == 204837 && var == 0)//Hresvelgr
      {
        switch(env.getDialogId())
					{
					case 25:
					 return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
          case 10000:
				   PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
           qs.setQuestVarById(0, var + 1);
				   updateQuestStatus(player, qs);
           return true;
          }
      }   
        
       
   return false;
   }
   return false; 
  }
  
}  
