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
package quest.eltnen;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
* @author MrPoke remod By Nephis and all quest team helper
*
*/
public class _1469FindingDenlavis extends QuestHandler
{
   private final static int   questId   = 1469;

   public _1469FindingDenlavis()
   {
      super(questId);
   }
   
    @Override
   public void register()
   {
      qe.setNpcQuestData(790004).addOnQuestStart(questId);
      qe.setNpcQuestData(790004).addOnTalkEvent(questId);
      qe.setNpcQuestData(212878).addOnTalkEvent(questId);
   }

   @Override
   public boolean onDialogEvent(QuestEnv env)
   {
      final Player player = env.getPlayer();
      int targetId = 0;
      if(env.getVisibleObject() instanceof Npc)
         targetId = ((Npc) env.getVisibleObject()).getNpcId();
      QuestState qs = player.getQuestStateList().getQuestState(questId);
      if(targetId == 790004)
      {
         if(qs == null || qs.getStatus() == QuestStatus.NONE)
         {
            if(env.getDialogId() == 25)
               return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);

               return defaultQuestStartDialog(env);
         }
         else if(qs.getStatus() == QuestStatus.START)
         {
            if(env.getDialogId() == 25)
               return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
            else if(env.getDialogId() == 1009)
            {
               qs.setQuestVar(2);
               qs.setStatus(QuestStatus.REWARD);
               updateQuestStatus(player, qs);
               return defaultQuestEndDialog(env);
            }
            else
               return defaultQuestEndDialog(env);
         }
         else if(qs.getStatus() == QuestStatus.REWARD)
         {
            return defaultQuestEndDialog(env);
         }
      }
      else if(targetId == 212878)
      {
         if(qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0)
         {
            if(env.getDialogId() == 25)
               return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
            else if(env.getDialogId() == 10000)
            {
               qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
               updateQuestStatus(player, qs);
               PacketSendUtility
                  .sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
               return true;
            }
            else
               return defaultQuestStartDialog(env);
         }
      }
      return false;
   }
}