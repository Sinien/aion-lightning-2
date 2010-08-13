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
package quest.beluslan;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author heldl
 *
 */
public class _2653SpyFindingBollvig extends QuestHandler
{
   private final static int   questId = 2653;
   
   public _2653SpyFindingBollvig()
   {
      super(questId);
   }
   
   @Override
   public void register()
   {
      qe.setNpcQuestData(204650).addOnQuestStart(questId);
      qe.setNpcQuestData(204650).addOnTalkEvent(questId);
      qe.setNpcQuestData(204655).addOnTalkEvent(questId);
      qe.setNpcQuestData(204775).addOnTalkEvent(questId);
   }
   
   public boolean onLvlUpEvent(QuestEnv env)
   {
      final Player player = env.getPlayer();
      final QuestState qs = player.getQuestStateList().getQuestState(questId);
      final QuestState qs2 = player.getQuestStateList().getQuestState(2652);
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
	   int targetId = 0;
	   if(env.getVisibleObject() instanceof Npc)
		   targetId = ((Npc) env.getVisibleObject()).getNpcId();
	   final QuestState qs = player.getQuestStateList().getQuestState(questId);
	   if(targetId == 204650)
	   {
		   if(qs == null)
		   {
			   if(env.getDialogId() == 25)
				   return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
			   else 
				   return defaultQuestStartDialog(env);
		   }
      }else if(targetId == 204655)
      {
    	  if(qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0)
    	  {
    		  if(env.getDialogId() == -1)
    		  {
    			  PacketSendUtility.sendMessage(player, "25");
    			  return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
    		  }
    		  else if(env.getDialogId() == 10000)
    		  {
    			  qs.setQuestVarById(0, qs.getQuestVarById(0)+1);
    			  updateQuestStatus(player, qs);
    			  PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
    			  return true;
    		  }
    		  else
    			return defaultQuestStartDialog(env);  
    	  }
      }else if(targetId == 204775)
      {
          if(qs != null)
          {
             if(env.getDialogId() == 25 && qs.getStatus() == QuestStatus.START)
                return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
             else if(env.getDialogId() == 1009)
             {
                qs.setQuestVar(3);
                qs.setStatus(QuestStatus.REWARD);
                updateQuestStatus(player, qs);
                return defaultQuestEndDialog(env);
             }
             else
                return defaultQuestEndDialog(env);
          }
      }
	   return false;
   }   
}
