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
package quest.beluslan;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.InstanceService;
import com.aionemu.gameserver.services.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author kecimis
 * 
 */
public class _4200ASuspiciousCall extends QuestHandler
{

	private final static int	questId	= 4200;
	private final static int[]	npc_ids	= { 204839, 798332, 700522, 279006, 204286 };

	/*
	 * 204839 - Uikinerk 798332 - Haorunerk 700522 - Haorunerks Bag 279006 - Garkbinerk 204286 - Payrinrinerk
	 */

	public _4200ASuspiciousCall()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(204839).addOnQuestStart(questId); // Uikinerk
		qe.setQuestItemIds(182209097).add(questId);// Teleport Scroll
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
			if(targetId == 204839)// Uikinerk
			{
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 4762);
				else
					return defaultQuestStartDialog(env);

			}
			return false;
		}

		int var = qs.getQuestVarById(0);

		if(qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 204286)// Payrinrinerk
			{
				if(env.getDialogId() == -1)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10002);
				else if(env.getDialogId() == 1009)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
				else
					return defaultQuestEndDialog(env);
			}
			return false;
		}
		else if(qs.getStatus() == QuestStatus.START)
		{
			if(targetId == 204839)// Uikinerk
			{
				switch(env.getDialogId())
				{
					case 25:
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1003);
					case 1011:
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
					case 10000:
						// Create instance
						WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(300100000);
						InstanceService.registerPlayerWithInstance(newInstance, player);
						// teleport to cell in steel rake: 300100000 403.55 508.11 885.77 0
						TeleportService.teleportTo(player, 300100000, newInstance.getInstanceId(), 403.55f, 508.11f,
							885.77f, 0);
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						return true;
				}

			}
			else if(targetId == 798332 && var == 1)// Haorunerk
			{
				switch(env.getDialogId())
				{
					case 25:
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
					case 10001:
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(player, qs);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(),
							10));
						return true;
				}
			}
			else if(targetId == 700522 && var == 2)// Haorunerks Bag, loc: 401.24 503.19 885.76 119
			{
				ThreadPoolManager.getInstance().schedule(new Runnable(){
					@Override
					public void run()
					{
						updateQuestStatus(player, qs);
					}
				}, 3000);
				return true;
			}
			else if(targetId == 279006 && var == 3)// Garkbinerk
			{
				switch(env.getDialogId())
				{
					case 25:
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
					case 10255:
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(),
							10));
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(player, qs);
						return true;

				}
			}
		}
		return false;
	}

	@Override
	public boolean onItemUseEvent(QuestEnv env, Item item)
	{
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		if(id != 182209097 || qs == null || qs.getQuestVarById(0) != 2)
			return false;

		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id,
			3000, 0, 0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable(){
			@Override
			public void run()
			{
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId,
					id, 0, 1, 0), true);
				player.getInventory().removeFromBagByItemId(182209097, 1);
				// teleport location(BlackCloudIsland): 400010000 3419.16 2445.43 2766.54 57
				TeleportService.teleportTo(player, 400010000, 3419.16f, 2445.43f, 2766.54f, 57);
				qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
				updateQuestStatus(player, qs);
			}
		}, 3000);
		return true;
	}
}
