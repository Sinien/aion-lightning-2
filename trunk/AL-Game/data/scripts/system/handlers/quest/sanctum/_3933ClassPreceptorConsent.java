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
 * @author Nanou
 * 
 */
public class _3933ClassPreceptorConsent extends QuestHandler
{
	private final static int	questId	= 3933;

	public _3933ClassPreceptorConsent()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(203701).addOnQuestStart(questId);	//Lavirintos
		qe.setNpcQuestData(203704).addOnTalkEvent(questId);		//Boreas
		qe.setNpcQuestData(203705).addOnTalkEvent(questId);		//Jumentis
		qe.setNpcQuestData(203706).addOnTalkEvent(questId);		//Charnas
		qe.setNpcQuestData(203707).addOnTalkEvent(questId);		//Thrasymedes
		qe.setNpcQuestData(203752).addOnTalkEvent(questId);		//Jucleas
		qe.setNpcQuestData(203701).addOnTalkEvent(questId);		//Lavirintos
	}

	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		// Instanceof
		final Player player = env.getPlayer();
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		
		// ------------------------------------------------------------
		// NPC Quest :
		// 0 - Start to Lavirintos
		if(qs == null || qs.getStatus() == QuestStatus.NONE) 
		{
			if(targetId == 203701)
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
				// 1 - Receive the signature of Boreas on the recommendation letter.
				case 203704:
					switch(env.getDialogId())
					{
						// Get HACTION_QUEST_SELECT in the eddit-HyperLinks.xml
						case 25:
							// Send select1 to eddit-HtmlPages.xml
							return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
						// Get HACTION_SETPRO1 in the eddit-HyperLinks.xml
						case 10000:
							qs.setQuestVarById(0, var + 1);
							updateQuestStatus(player, qs);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
				// 2 - Receive the signature of Jumentis on the recommendation letter.
				case 203705:
					if(var == 1)
					{
						switch(env.getDialogId())
						{
							// Get HACTION_QUEST_SELECT in the eddit-HyperLinks.xml
							case 25:
								// Send select2 to eddit-HtmlPages.xml
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
							// Get HACTION_SETPRO2 in the eddit-HyperLinks.xml
							case 10001:
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
					}
				// 3 - Receive the signature of Charna on the recommendation letter.
				case 203706:
					if(var == 2)
					{
						switch(env.getDialogId())
						{
							// Get HACTION_QUEST_SELECT in the eddit-HyperLinks.xml
							case 25:
								// Send select3 to eddit-HtmlPages.xml
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
							// Get HACTION_SETPRO3 in the eddit-HyperLinks.xml
							case 10002:
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
					}
				// 4 - Receive the signature of Thrasymedes on the recommendation letter.
				case 203707:
					if(var == 3)
					{
						switch(env.getDialogId())
						{
							// Get HACTION_QUEST_SELECT in the eddit-HyperLinks.xml
							case 25:
								// Send select4 to eddit-HtmlPages.xml
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
							// Get HACTION_SETPRO4 in the eddit-HyperLinks.xml
							case 10003:
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
					}
				// 5 - Report the result to Lavirintos with the Oath Stone
				case 203752:
					if(var == 4)
					{
						switch(env.getDialogId())
						{
							// Get HACTION_QUEST_SELECT in the eddit-HyperLinks.xml
							case 25:
								if(player.getInventory().getItemCountByItemId(186000080) >= 1)
									// Send select5 to eddit-HtmlPages.xml
									return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
								else
									// Send select5_2 to eddit-HtmlPages.xml
									return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2461);
							// Get HACTION_SET_SUCCEED in the eddit-HyperLinks.xml
							case 10255:
									// Send select_success to eddit-HtmlPages.xml
									return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10002);
							// Get HACTION_SELECT_QUEST_REWARD in the eddit-HyperLinks.xml
							case 1009:
									player.getInventory().removeFromBagByItemId(186000080, 1);	
									qs.setStatus(QuestStatus.REWARD);
									updateQuestStatus(player, qs);	
									// Send select_quest_reward1 to eddit-HtmlPages.xml									
									return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
						}
					}
			}
		}
		else if(qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 203701)
				return defaultQuestEndDialog(env);
		}
	return false;
	}
}