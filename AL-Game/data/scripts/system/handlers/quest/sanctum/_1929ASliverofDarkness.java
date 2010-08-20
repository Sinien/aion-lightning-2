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
package quest.sanctum;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Equipment;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.SystemMessageId;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.InstanceService;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Mr. Poke, edited Rolandas
 *
 */
public class _1929ASliverofDarkness extends QuestHandler
{

	private final static int		questId	= 1929;

	public _1929ASliverofDarkness()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.addQuestLvlUp(questId);
		qe.setNpcQuestData(203752).addOnTalkEvent(questId);
		qe.setNpcQuestData(203852).addOnTalkEvent(questId);
		qe.setNpcQuestData(203164).addOnTalkEvent(questId);
		qe.setNpcQuestData(205110).addOnTalkEvent(questId);
		qe.setNpcQuestData(700419).addOnTalkEvent(questId);
		qe.setNpcQuestData(205111).addOnTalkEvent(questId);
		qe.setQuestMovieEndIds(155).add(questId);
		qe.setNpcQuestData(212992).addOnKillEvent(questId);
		qe.setNpcQuestData(203701).addOnTalkEvent(questId);
		qe.setNpcQuestData(203711).addOnTalkEvent(questId);
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
				case 203752:
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
				case 203852:
					switch(env.getDialogId())
					{
						case 25:
							if(var == 1)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
						case 10001:
							if(var == 1)
							{
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
					break;
				case 203164:
					switch(env.getDialogId())
					{
						case 25:
							if(var == 2)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
							else if(var == 8)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 3057);
							break;
						case 10002:
							if(var == 2)
							{
								qs.setQuestVar(93);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
								WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(310070000);
								InstanceService.registerPlayerWithInstance(newInstance, player);
								TeleportService.teleportTo(player, 310070000, newInstance.getInstanceId(), 338, 101, 1191, 0);
								return true;
							}
							break;
						case 10006:
						if (var == 8)
						{
							removeStigma(player);
							qs.setQuestVarById(0, var + 1);
							updateQuestStatus(player, qs);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
					}
					break;
				case 205110:
					switch(env.getDialogId())
					{
						case 25:
							if(var == 93)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
						case 10003:
							if(var == 93)
							{
								qs.setQuestVar(94);
								updateQuestStatus(player, qs);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
								PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 31001, 0));
								return true;
							}
					}
					break;
				case 700419:
				{
					if (qs.getQuestVars().getQuestVars() == 94 && env.getDialogId() == -1)
					{
						final int targetObjectId = env.getVisibleObject().getObjectId();
						PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000,
							1));
						PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.NEUTRALMODE2, 0,
							targetObjectId), true);
						ThreadPoolManager.getInstance().schedule(new Runnable(){
							@Override
							public void run()
							{
								if(!player.isTargeting(targetObjectId))
									return;
								PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(),
									targetObjectId, 3000, 0));
								PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_LOOT, 0,
									targetObjectId), true);

								PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 155));
							}
						}, 3000);
					}
				}
				break;
				case 205111:
					switch(env.getDialogId())
					{
						case -1:
							if(var == 98)
							{
								int itemId = getStoneId(player);
								if (player.getEquipment().getEquippedItemsByItemId(itemId).size() != 0)
								{
									qs.setQuestVar(96);
									updateQuestStatus(player, qs);
								}
								return false;
							}
							break;
						case 25:
							if(var == 98)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
							else if(var == 96)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2716);
							break;
						case 2546:
							if(var == 98)
							{
								int itemId = getStoneId(player);
								if (player.getInventory().getItemCountByItemId(itemId) > 0)
								{
									PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 1));
									return true;
								}
								List<QuestItems> items = new ArrayList<QuestItems>();
								items.add(new QuestItems(itemId, 1));
								items.add(new QuestItems(141000001, 60));
								if (ItemService.addItems(player, items))
								{
									PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 1));
								}
								return true;
							}
							break;
						case 2720:
							if(var == 96)
							{
								Npc npc = (Npc) env.getVisibleObject();
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
								npc.getController().delete();
								ThreadPoolManager.getInstance().schedule(new Runnable(){
									@Override
									public void run()
									{
										QuestService.addNewSpawn(310070000, instanceId, 212992, (float)191.9, (float)267.68, (float)1374, (byte) 0, true);
										qs.setQuestVar(97);
										updateQuestStatus(player, qs);
									}
								}, 5000);
								return true;
							}
					}
					break;
				case 203701:
					if (var == 9)
					{
						switch(env.getDialogId())
						{
							case 25:
								if(var == 9)
									return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 3398);
							case 10007:
								if(var == 9)
								{
									qs.setStatus(QuestStatus.REWARD);
									updateQuestStatus(player, qs);
									PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
									return true;
								}
						}
						break;
					}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD && targetId == 203711)
		{
			if (env.getDialogId() == -1)
				return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 10002);
			return defaultQuestEndDialog(env);
		}
		
		return false;
	}

	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId)
	{
		if(movieId != 155)
			return false;
		Player player = env.getPlayer();
		int instanceId = player.getInstanceId();
		env.setQuestId(questId);
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.START || qs.getQuestVars().getQuestVars() != 94)
			return false;
		QuestService.addNewSpawn(310070000, instanceId, 205111, (float) 197.6, (float) 265.9, (float) 1374.0, (byte) 0, true);
		qs.setQuestVar(98);
		updateQuestStatus(player, qs);
		return true;
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
	public boolean onKillEvent(QuestEnv env)
	{
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(targetId == 212992 && qs.getQuestVars().getQuestVars() == 97)
		{
			qs.setQuestVar(8);
			updateQuestStatus(player, qs);
			ThreadPoolManager.getInstance().schedule(new Runnable(){
				@Override
				public void run()
				{
					TeleportService.teleportTo(player, 210030000, 1, 2315.9f, 1800f, 195.2f, 0);
				}
			}, 5000);
			return true;
		}
		return false;
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
			qs.setQuestVar(2);
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
				if(player.getWorldId() != 310070000)
				{
					removeStigma(player);
					qs.setQuestVar(2);
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
		ItemService.removeItemFromInventoryByItemId(player, itemId);
	}
}
