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

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
/**
 * @author Rhys2002
 * edited by xaerolt
 */
public class _1042KeeperoftheKaidanKey extends QuestHandler
{
	private final static int	questId	= 1042;
	private final static int[]	npc_ids	= { 203989, 203901};
	private final static int[]      mob_ids	= { 212029, 212033 }; //Kaidan Blocker Boss, Crack Kaidan Captain              //
	
	public _1042KeeperoftheKaidanKey()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.addQuestLvlUp(questId);
		for(int npc_id : npc_ids)
			qe.setNpcQuestData(npc_id).addOnTalkEvent(questId);
		qe.setQuestItemIds(182201018).add(questId);	                     ////////////// fix of q item id	 ///////////////////
		for(int mob_id : mob_ids)                                            //
			qe.setNpcQuestData(mob_id).addOnKillEvent(questId);            //  
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		boolean lvlCheck = QuestService.checkLevelRequirement(questId, player.getCommonData().getLevel());
		if(qs == null || qs.getStatus() != QuestStatus.LOCKED || !lvlCheck)
			return false;
		QuestState qs2 = player.getQuestStateList().getQuestState(1040);
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
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 203901)
			{
				if(env.getDialogId() == -1)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10002);
				else if(env.getDialogId() == 1009)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
				else return defaultQuestEndDialog(env);
			}
			return false;
		}
		else if(qs.getStatus() != QuestStatus.START)
		{
			return false;
		}
		if(targetId == 203989)
		{
			switch(env.getDialogId())
			{
				case 25:
					if(var == 0)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
				case 1012:
					PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 185));
						break;
				case 10000:
					if(var == 0)
					{
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
			}
		}
		else if(targetId == 203901)
		{
			switch(env.getDialogId())
			{
				case 25:
					if(var == 2)
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
				case 33:
					if(QuestService.collectItemCheck(env, true))				
					{                             
						player.getInventory().removeFromBagByItemId(182201018, 1);           ///// nobody needs useless junk ///
						qs.setStatus(QuestStatus.REWARD);					
						updateQuestStatus(player, qs);			
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
					}
					else
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1438);				
			}
		}		
		return false;
	}
	
	@Override
	public boolean onItemUseEvent(QuestEnv env, Item item)           // quest can be completed by using the needed key if it was gained before this fix
	{
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();
   		if(id != 182201018)                                        //// for quest state change didn't activate without the required item ////
			return false;                                      
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 20, 1, 0), true);	
		SetQuestStatus2(player, env, qs);	
		return true;
	}

	@Override
	public boolean onKillEvent(QuestEnv env)       // implements(at least partly) finding who has the key from monsters /////////////////
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.START)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		switch(targetId)
		{
			case 212029:
			{
				if(var < 2) 
				{
					SetQuestStatus2(player, env, qs);
					return true;
				}
			} break;
			case 212033:
			{
				if(var < 2) 
				{
					SetQuestStatus2(player, env, qs);
					return true;
				}
			}
		}
		return false;
	}

	private void SetQuestStatus2(Player player, QuestEnv env, QuestState qs) {
		qs.setQuestVarById(0,2);
		updateQuestStatus(player, qs);
	}
}