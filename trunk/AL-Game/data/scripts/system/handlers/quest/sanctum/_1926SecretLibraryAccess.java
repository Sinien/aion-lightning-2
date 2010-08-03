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
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapType;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.services.TeleportService;

/**
 * @author xaerolt, Rolandas
 *
 */
public class _1926SecretLibraryAccess extends QuestHandler {
	private final static int questId	= 1926;
	private final static int[] npc_ids	= { 203894, 203098 }; // 203894 - Latri(start and finish), 203098 - Spatalos(for recomendation)

	public _1926SecretLibraryAccess()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.setNpcQuestData(203894).addOnQuestStart(questId);
		for(int npc_id : npc_ids) {
			qe.setNpcQuestData(npc_id).addOnTalkEvent(questId);
		}
	}

	//// self explanatory ////////////////////////////////////////////////////////////////////
	private boolean AreVerteronQuestsFinished(Player player) {
		QuestState qs = player.getQuestStateList().getQuestState(1020); // last quest in Verteron state
		return ((qs == null) || (qs.getStatus() != QuestStatus.COMPLETE)) ? false : true;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		if(targetId == 203894)
		{
			if(qs == null || qs.getStatus() == QuestStatus.NONE) {
				if(env.getDialogId() == 25)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 4762);
				else return defaultQuestStartDialog(env);
			} else if (qs.getStatus() == QuestStatus.REWARD && qs.getQuestVarById(0) == 0 ||
				       qs.getStatus() == QuestStatus.COMPLETE) {
				if(env.getDialogId() == -1 && qs.getStatus() == QuestStatus.REWARD)
					return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10002);
				else if (env.getDialogId() == 17) {
					player.getInventory().removeFromBagByItemId(182206022, 1);
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(player, qs);
					return defaultQuestEndDialog(env);
				} else if (env.getDialogId() == 1009) {
					return defaultQuestEndDialog(env);
				}
				ThreadPoolManager.getInstance().schedule(new Runnable()
				{
					@Override
					public void run()
					{
						TeleportService.teleportTo(player, WorldMapType.SANCTUM.getId(), 2032.9f, 1473.1f, 592.2f, 195);
					}
				}, 3000);
			}
		} else if(targetId == 203098)
		{
			if(qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0)
			{
				if(env.getDialogId() == 25) {
					if(AreVerteronQuestsFinished(player)) {
						return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
					} else return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1097);
				}
				else if(env.getDialogId() == 10255) {
					if(ItemService.addItems(player, Collections.singletonList(new QuestItems(182206022, 1)))) {
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(player, qs);
					}
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
					return true;
				}
				else return defaultQuestStartDialog(env);
			}
		}
		return false;
	}
}
