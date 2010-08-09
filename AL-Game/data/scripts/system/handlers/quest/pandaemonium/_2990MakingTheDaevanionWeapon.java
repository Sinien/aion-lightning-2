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
 */
public class _2990MakingTheDaevanionWeapon extends QuestHandler
{
	private final static int	questId	= 2990;
  private final static int[]	npc_ids	= { 204146 };
  
  int ALL = 0; 
  int A = 0;
  int B = 0;
  int C = 0;
   /*
   * 204146 - Kanensa
   *             
   */
	
  public _2990MakingTheDaevanionWeapon()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(204146).addOnQuestStart(questId);//Kanensa
		qe.setNpcQuestData(256617).addOnKillEvent(questId);//Strange Lake Spirit
		qe.setNpcQuestData(253720).addOnKillEvent(questId);//Lava Hoverstone
		qe.setNpcQuestData(254513).addOnKillEvent(questId);//Disturbed Resident
    for(int npc_id : npc_ids)
			qe.setNpcQuestData(npc_id).addOnTalkEvent(questId);
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
			if(targetId == 204146)//Kanensa
			{
				if(env.getDialogId() == 25)
				{
					int plate = player.getEquipment().itemSetPartsEquipped(9);
          int chain = player.getEquipment().itemSetPartsEquipped(8);
          int leather = player.getEquipment().itemSetPartsEquipped(7);
          int cloth = player.getEquipment().itemSetPartsEquipped(6);
          
          if (plate != 5 && chain != 5 && leather != 5 && cloth != 5)
           return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 4848);
          else
          return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 4762);
        }
				else
					return defaultQuestStartDialog(env);
			}
		}
		
		if(qs == null)
			return false;
			
		int var = qs.getQuestVarById(0);
    int var1 = qs.getQuestVarById(1);			
    
        
		if(qs.getStatus() == QuestStatus.START)
		{
		 if (targetId == 204146)
		 {
      switch (env.getDialogId())
      {
       case 25:
        if (var == 0)
         return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
        if (var == 1)
         return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
        if (var == 2 && var1 == 60)
         return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
        if (var == 3 && player.getInventory().getItemCountByItemId(186000040) > 0)
         return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
       case 33:
        if (var == 0)
        {
         if(QuestService.collectItemCheck(env, true))				
				 {
					qs.setQuestVarById(0, var + 1);					
				  updateQuestStatus(player, qs);
          return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10000);
				 }
         else
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10001);
        }
       break;
       case 1352:
        if (var == 0)
         return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
       case 2035:
        if (var == 3 && player.getInventory().getItemCountByItemId(186000040) > 0)
        {
         if(player.getCommonData().getDp() < 4000)
         {
          return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2120);
         }
         else
         {
          player.getInventory().removeFromBagByItemId(186000040, 1);
          player.getCommonData().setDp(0);
          qs.setStatus(QuestStatus.REWARD);
			    updateQuestStatus(player, qs);
			    return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
         }
        }
       break;
       case 10001:
        if (var == 1)
        {
         qs.setQuestVarById(0, var + 1);					
				 updateQuestStatus(player, qs);
				 PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
				 return true;
        }
       break;
       case 10002:
        if (var == 2)
        {
         qs.setQuestVarById(0, var + 1);					
				 updateQuestStatus(player, qs);
				 PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
				 return true;
        }
       break;
      }
     }
     return false;
    }
    else if (qs.getStatus() == QuestStatus.REWARD)
    {
     if(targetId == 204146)//Kanensa
			{
			  return defaultQuestEndDialog(env);
      }
      return false;
    }
  return false;
  }
  @Override
  public boolean onKillEvent(QuestEnv env)
	{
	 final Player player = env.getPlayer();
	 QuestState qs = player.getQuestStateList().getQuestState(questId);
	 if(qs == null || qs.getStatus() != QuestStatus.START)
		return false;
    
	 int targetId = 0;
	 if(env.getVisibleObject() instanceof Npc)
		 targetId = ((Npc) env.getVisibleObject()).getNpcId();			
      
   
   
   if((targetId == 256617 || targetId == 253720 || targetId == 254513) && qs.getQuestVarById(0) == 2 )
   {
     switch(targetId)
     {
      case 256617:
       if (A >= 0 && A < 30)
       {
        ++A;
        ALL = C;
        ALL = ALL << 7;
        ALL += B;
        ALL = ALL << 7;
        ALL += A;
        ALL = ALL << 7;
        ALL += 2;//var0 
        qs.setQuestVar(ALL);
        updateQuestStatus(player, qs);
			 }
  	  break;
      case 253720:
       if (B >= 0 && B < 30)
       {
        ++B;
        ALL = C;
        ALL = ALL << 7;
        ALL += B;
        ALL = ALL << 7;
        ALL += A;
        ALL = ALL << 7;
        ALL += 2;//var0
        qs.setQuestVar(ALL);
			  updateQuestStatus(player, qs);
			 }
  	  break;
      case 254513:
       if (C >= 0 && C < 30)
       {
        ++C;
        ALL = C;
        ALL = ALL << 7;
        ALL += B;
        ALL = ALL << 7;
        ALL += A;
        ALL = ALL << 7;
        ALL += 2;//var0
        qs.setQuestVar(ALL);
			  updateQuestStatus(player, qs);
			 }
  	  break;
     }
   }
   
   if (qs.getQuestVarById(0) == 2 && A == 30 && B == 30 && C == 30)
   {
    qs.setQuestVarById(1, 60);					
		updateQuestStatus(player, qs);
		return true;
   }
  return false;
  }
}

