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

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author JIEgOKOJI, fixed by kecimis
 * 
 */
public class _4934TheShulacksStigma extends QuestHandler
{
	private final static int questId = 4934;
	

	public _4934TheShulacksStigma()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(204051).addOnQuestStart(questId); //Vergelmir start
		qe.setNpcQuestData(204211).addOnTalkEvent(questId);	//Moreinen
		qe.setNpcQuestData(204285).addOnTalkEvent(questId);	//Teirunerk
		qe.setNpcQuestData(700562).addOnTalkEvent(questId); //
    qe.setQuestItemIds(182207102).add(questId);				//Heavy Surkana
		qe.setNpcQuestData(204051).addOnTalkEvent(questId);		// Vergelmir
	}

	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		// Instanceof
		final Player player = env.getPlayer();
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		
		// ------------------------------------------------------------
		// NPC Quest :
		// 0 - Vergelmir start
		if(qs == null || qs.getStatus() == QuestStatus.NONE) 
		{
			if(targetId == 204051)
			{
				// Get HACTION_QUEST_SELECT in the eddit-HyperLinks.xml
				if(env.getDialogId() == 25)
					// Send HTML_PAGE_SELECT_NONE to eddit-HtmlPages.xml
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 4762);
				else
					return defaultQuestStartDialog(env);

			}
		}
		
		if(qs == null)
			return false;
		
		int var = qs.getQuestVarById(0);			

		if(qs.getStatus() == QuestStatus.START)
		{
			
			switch(targetId)
			{
				  
        //Moreinen
        case 204211:
					if(var == 0)
					{
						switch(env.getDialogId())
						{
							// Get HACTION_QUEST_SELECT in the eddit-HyperLinks.xml
							case 25:
								// Send select1 to eddit-HtmlPages.xml
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
							// Get HACTION_SETPRO1 in the eddit-HyperLinks.xml
							case 10000:
								qs.setQuestVar(1);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
					}
				// 2 / 4- Talk with Teirunerk
				case 204285:
					if(var == 1)
					{
						switch(env.getDialogId())
						{
							// Get HACTION_QUEST_SELECT in the eddit-HyperLinks.xml
							case 25:
							// Send select1 to eddit-HtmlPages.xml
							return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
							// Get HACTION_SETPRO1 in the eddit-HyperLinks.xml
							case 10001:
								qs.setQuestVar(2);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
					}
					else if(var == 2)
					{
						switch(env.getDialogId())
						{
							// Get HACTION_QUEST_SELECT in the eddit-HyperLinks.xml
							case 25:
							// Send select1 to eddit-HtmlPages.xml
							return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
							// Get HACTION_SETPRO1 in the eddit-HyperLinks.xml
							case 33:
								if(player.getInventory().getItemCountByItemId(182207102) < 1)
								{
									// player doesn't own required item
									return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10001);
								}
								ItemService.removeItemFromInventoryByItemId(player, 182207102);	
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(player, qs);	
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10000);
						}
					
          }
          return false;
					case 700562:
			   if (env.getDialogId() == -1)
					{
						final int targetObjectId = env.getVisibleObject().getObjectId();
				    ThreadPoolManager.getInstance().schedule(new Runnable(){
					  @Override
					  public void run()
					   {
              if(!player.isTargeting(targetObjectId))
              return;
              PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, targetObjectId), true);
              ItemService.addItems(player, Collections.singletonList(new QuestItems(182207102, 1)));
              updateQuestStatus(player, qs);
		         }
             }, 3000);
					  return true; 
          }
      }
		}
		else if(qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 204051)
				{
        if(env.getDialogId() == -1)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10002);
				else if(env.getDialogId() == 1009)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
				else return defaultQuestEndDialog(env);
				}
		}
	return false;
	}
}
