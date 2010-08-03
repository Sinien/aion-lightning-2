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
package quest.pandaemonium;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Equipment;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.SystemMessageId;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.InstanceService;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Mr. Poke, edited Rolandas
 *
 */
public class _2900NoEscapingDestiny extends QuestHandler
{

	private final static int		questId	= 2900;

	public _2900NoEscapingDestiny()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.addQuestLvlUp(questId);
		qe.setNpcQuestData(204182).addOnTalkEvent(questId);
		qe.setNpcQuestData(203550).addOnTalkEvent(questId);
		qe.setNpcQuestData(790003).addOnTalkEvent(questId);
		qe.setNpcQuestData(790002).addOnTalkEvent(questId);
		qe.setNpcQuestData(203546).addOnTalkEvent(questId);
		qe.setNpcQuestData(204264).addOnTalkEvent(questId);
		qe.setQuestMovieEndIds(156).add(questId);
		qe.setNpcQuestData(204263).addOnKillEvent(questId);
		qe.setNpcQuestData(204061).addOnTalkEvent(questId);
		qe.addOnEnterWorld(questId);
		qe.addOnDie(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		final int instanceId = player.getInstanceId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null)
			return false;

		int var = qs.getQuestVars().getQuestVars();
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(qs.getStatus() == QuestStatus.START)
		{
			switch (targetId)
			{
				case 204182:
					switch(env.getDialogId())
					{
						case 25:
							if(var == 0)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
						case 10000:
							if(var == 0)
							{
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
					break;
				case 203550:
					switch(env.getDialogId())
					{
						case 25:
							if(var == 1)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
							if(var == 10)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 4080);
						case 10001:
							if(var == 1)
							{
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
							break;
						case 10009:
							if(var == 10)
							{
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
					break;
				case 790003:
					switch(env.getDialogId())
					{
						case 25:
							if(var == 2)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
						case 10002:
							if(var == 2)
							{
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
					break;
				case 790002:
					switch(env.getDialogId())
					{
						case 25:
							if(var == 3)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
						case 10003:
							if(var == 3)
							{
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
					break;
				case 203546:
					switch(env.getDialogId())
					{
						case 25:
							if(var == 4)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
							else if(var == 9)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 3739);
							break;
						case 10004:
							if(var == 4)
							{
								qs.setQuestVar(95);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
								WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(320070000);
								InstanceService.registerPlayerWithInstance(newInstance, player);
								TeleportService.teleportTo(player, 320070000, newInstance.getInstanceId(), 257.5f, 245f, 129f, 0);
								return true;
							}
							break;
						case 10008:
							if (var == 9)
							{
								removeStigma(player);
								qs.setQuestVar(10);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
								return true;
							}
							break;
					}
					break;
				case 204264:
					switch(env.getDialogId())
					{
						case -1:
							if(var == 99)
							{
								int itemId = getStoneId(player);
								if (player.getEquipment().getEquippedItemsByItemId(itemId).size() != 0)
								{
									qs.setQuestVar(97);
									updateQuestStatus(player, qs);
								}
								return false;
							}
							break;
						case 25:
							if(var == 95)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2716);
							else if(var == 99)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 3057);
							else if(var == 97)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 3398);

							break;
						case 10005:
							if (var == 95)
							{
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
								PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 156));
								return true;
							}
							break;
						case 10007:
							if (var == 97)
							{
								qs.setQuestVar(98);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
								QuestService.addNewSpawn(320070000, instanceId, 204263, 257.5f, 245f, 129f, (byte) 0, true);
								return true;
							}
							break;
						case 3058:
							if(var == 99)
							{
								int itemId = getStoneId(player);
								if (player.getInventory().getItemCountByItemId(itemId) > 0)
									return false;
								List<QuestItems> items = new ArrayList<QuestItems>();
								items.add(new QuestItems(itemId, 1));
								items.add(new QuestItems(141000001, 60));
								if (!ItemService.addItems(player, items))
									return true;
								else
									return false;
							}
							break;
						case 10006:
							if(var == 99)
							{
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 1));
								return true;
							}
					}
					break;
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD && targetId == 204061)
			return defaultQuestEndDialog(env);
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

		if(targetId == 204263 && qs.getQuestVars().getQuestVars() == 98)
		{
			qs.setQuestVar(9);
			updateQuestStatus(player, qs);
			TeleportService.teleportTo(player, 220010000, 1, 1111.6f, 1716.6f, 270.6f, 0);
			return true;
		}
		return false;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs != null)
			return false;
		boolean lvlCheck = QuestService.checkLevelRequirement(questId, player.getCommonData().getLevel());
		if(!lvlCheck)
			return false;
		env.setQuestId(questId);
		QuestService.startQuest(env, QuestStatus.START);
		return true;
	}

	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId)
	{
		if(movieId != 156)
			return false;
		Player player = env.getPlayer();
		env.setQuestId(questId);
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.START || qs.getQuestVars().getQuestVars() != 95)
			return false;
		qs.setQuestVar(99);
		updateQuestStatus(player, qs);
		return true;
	}

	@Override
	public boolean onDieEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.START)
			return false;
		int var = qs.getQuestVars().getQuestVars();
		if(var > 90)
		{
			removeStigma(player);
			qs.setQuestVar(4);
			updateQuestStatus(player, qs);
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(SystemMessageId.QUEST_FAILED_$1, DataManager.QUEST_DATA.getQuestById(questId).getName()));
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs != null && qs.getStatus() == QuestStatus.START)
		{
			int var = qs.getQuestVars().getQuestVars();
			if(var > 90)
			{
				if(player.getWorldId() != 320070000)
				{
					removeStigma(player);
					qs.setQuestVar(4);
					updateQuestStatus(player, qs);
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(SystemMessageId.QUEST_FAILED_$1, DataManager.QUEST_DATA.getQuestById(questId).getName()));
				}
			}
		}
		return false;
	}

	private int getStoneId(Player player)
	{
		switch(player.getCommonData().getPlayerClass())
		{
			case GLADIATOR:
				return 140000008; // Improved Stamina I
			case TEMPLAR:
				return 140000027; // Divine Fury I
			case RANGER:
				return 140000047; // Arrow Deluge I
			case ASSASSIN:
				return 140000076; // Sigil Strike I
			case SORCERER:
				return 140000131; // Lumiel's Wisdom I
			case SPIRIT_MASTER:
				return 140000147; // Absorb Vitality I
			case CLERIC:
				return 140000098; // Grace of Empyrean Lord I
			case CHANTER:
				return 140000112; // Rage Spell I
		}
		return 0;
	}
	
	private void removeStigma(Player player)
	{
		int itemId = getStoneId(player);
		List<Item> items = player.getEquipment().getEquippedItemsByItemId(itemId);
		Equipment equipment = player.getEquipment();
		for (Item item : items)
		{
			equipment.unEquipItem(item.getObjectId(), 0);
		}
		player.getInventory().removeFromBagByItemId(itemId, 1);
	}
}
