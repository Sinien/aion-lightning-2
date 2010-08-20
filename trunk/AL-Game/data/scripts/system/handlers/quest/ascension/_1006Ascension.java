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
package quest.ascension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.stats.StatEnum;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.network.aion.SystemMessageId;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ASCENSION_MORPH;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
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
import com.aionemu.gameserver.services.ZoneService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author MrPoke
 * 
 */
public class _1006Ascension extends QuestHandler
{
	private final static int		questId	= 1006;
	
	public _1006Ascension()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		if(CustomConfig.ENABLE_SIMPLE_2NDCLASS)
			return;
		qe.addQuestLvlUp(questId);
		qe.setNpcQuestData(790001).addOnTalkEvent(questId);
		qe.setQuestItemIds(182200007).add(questId);
		qe.setNpcQuestData(730008).addOnTalkEvent(questId);
		qe.setNpcQuestData(205000).addOnTalkEvent(questId);
		qe.setNpcQuestData(211042).addOnKillEvent(questId);
		qe.setNpcQuestData(211043).addOnAttackEvent(questId);
		qe.setQuestMovieEndIds(151).add(questId);
		qe.addOnEnterWorld(questId);
		qe.addOnDie(questId);
		qe.addOnQuestFinish(questId);
		deletebleItems = new int[]{182200008, 182200009};
	}

	@Override
	public boolean onKillEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		int instanceId = player.getInstanceId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if(targetId == 211042)
		{
			if(var >= 51 && var <= 53)
			{
				qs.setQuestVar(qs.getQuestVars().getQuestVars() + 1);
				updateQuestStatus(player, qs);
				return true;
			}
			else if(var == 54)
			{
				qs.setQuestVar(4);
				updateQuestStatus(player, qs);
				Npc mob = (Npc) QuestService.addNewSpawn(310010000, instanceId, 211043, (float) 226.7, (float) 251.5, (float) 205.5, (byte) 0, true);
				// TODO: Tempt decrease P attack.
				mob.getGameStats().setStat(StatEnum.MAIN_HAND_POWER, mob.getGameStats().getCurrentStat(StatEnum.MAIN_HAND_POWER) / 3);
				mob.getAggroList().addDamage(player, 1000);
				return true;
			}
		}
		return false;
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
			if(targetId == 790001)
			{
				switch(env.getDialogId())
				{
					case 25:
						if(var == 0)
							return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1011);
						else if(var == 3)
							return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1693);
						else if(var == 5)
							return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2034);
					case 10000:
						if(var == 0)
						{
							if (!ItemService.addItems(player, Collections.singletonList(new QuestItems(182200007, 1))))
								return true;
							qs.setQuestVarById(0, var + 1);
							updateQuestStatus(player, qs);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
					case 10002:
						if(var == 3)
						{
							ItemService.removeItemFromInventoryByItemId(player, 182200009);
							qs.setQuestVar(99);
							updateQuestStatus(player, qs);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
							WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(310010000);
							InstanceService.registerPlayerWithInstance(newInstance, player);
							TeleportService.teleportTo(player, 310010000, newInstance.getInstanceId(), 52, 174, 229, 0);
							return true;
						}
					case 10003:
						if(var == 5)
						{
							PlayerClass playerClass = player.getCommonData().getPlayerClass();
							if(playerClass == PlayerClass.WARRIOR)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2375);
							else if(playerClass == PlayerClass.SCOUT)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 2716);
							else if(playerClass == PlayerClass.MAGE)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 3057);
							else if(playerClass == PlayerClass.PRIEST)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 3398);
						}
					case 10004:
						if(var == 5)
							return setPlayerClass(env, qs, PlayerClass.GLADIATOR);
					case 10005:
						if(var == 5)
							return setPlayerClass(env, qs, PlayerClass.TEMPLAR);
					case 10006:
						if(var == 5)
							return setPlayerClass(env, qs, PlayerClass.ASSASSIN);
					case 10007:
						if(var == 5)
							return setPlayerClass(env, qs, PlayerClass.RANGER);
					case 10008:
						if(var == 5)
							return setPlayerClass(env, qs, PlayerClass.SORCERER);
					case 10009:
						if(var == 5)
							return setPlayerClass(env, qs, PlayerClass.SPIRIT_MASTER);
					case 10010:
						if(var == 5)
							return setPlayerClass(env, qs, PlayerClass.CLERIC);
					case 10011:
						if(var == 5)
							return setPlayerClass(env, qs, PlayerClass.CHANTER);
				}
			}
			else if(targetId == 730008)
			{
				switch(env.getDialogId())
				{
					case 25:
						if(var == 2)
						{
							if(player.getInventory().getItemCountByItemId(182200008) != 0)
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1352);
							else
								return sendQuestDialog(player, env.getVisibleObject().getObjectId(), 1354);
						}
					case 1353:
						if(var == 2)
						{
							PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 14));
							ItemService.decreaseItemCountByItemId(player, 182200008, 1);
							ItemService.addItems(player, Collections.singletonList(new QuestItems(182200009, 1)));
						}
						return false;
					case 10001:
						if(var == 2)
						{
							qs.setQuestVarById(0, var + 1);
							updateQuestStatus(player, qs);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
				}
			}
			else if(targetId == 205000)
			{
				switch(env.getDialogId())
				{
					case 25:
						if(var == 99)
						{
							PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 1001, 0));
							qs.setQuestVar(50);
							updateQuestStatus(player, qs);
							ThreadPoolManager.getInstance().schedule(new Runnable(){
								@Override
								public void run()
								{
									qs.setQuestVar(51);
									updateQuestStatus(player, qs);
									List<Npc> mobs = new ArrayList<Npc>();
									mobs.add((Npc) QuestService.addNewSpawn(310010000, instanceId, 211042, (float) 224.073, (float) 239.1, (float) 206.7, (byte) 0, true));
									mobs.add((Npc) QuestService.addNewSpawn(310010000, instanceId, 211042, (float) 233.5, (float) 241.04, (float) 206.365, (byte) 0, true));
									mobs.add((Npc) QuestService.addNewSpawn(310010000, instanceId, 211042, (float) 229.6, (float) 265.7, (float) 205.7, (byte) 0, true));
									mobs.add((Npc) QuestService.addNewSpawn(310010000, instanceId, 211042, (float) 222.8, (float) 262.5, (float) 205.7, (byte) 0, true));
									for(Npc mob : mobs)
									{
										// TODO: Tempt decrease P attack.
										mob.getGameStats().setStat(StatEnum.MAIN_HAND_POWER, mob.getGameStats().getCurrentStat(StatEnum.MAIN_HAND_POWER) / 3);
										mob.getGameStats().setStat(StatEnum.PHYSICAL_DEFENSE, 0);
										mob.getAggroList().addDamage(player, 1000);
									}
								}
							}, 43000);
							return true;
						}
						return false;
					default:
						return false;
				}
			}
		}
		else if(qs.getStatus() == QuestStatus.REWARD)
		{
			if(targetId == 790001)
			{
				return defaultQuestEndDialog(env);
			}
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
	public boolean onAttackEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.START || qs.getQuestVars().getQuestVars() != 4)
			return false;
		int targetId = 0;
		if(env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		if(targetId != 211043)
			return false;
		Npc npc = (Npc) env.getVisibleObject();
		if(npc.getLifeStats().getCurrentHp() < npc.getLifeStats().getMaxHp() / 2)
		{
			PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(0, 151));
			npc.getController().onDelete();
		}
		return false;
	}

	@Override
	public boolean onItemUseEvent(QuestEnv env, final Item item)
	{
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();

		if(id != 182200007)
			return false;
		if(!ZoneService.getInstance().isInsideZone(player, ZoneName.ITEMUSE_Q1006))
			return false;
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null)
			return false;
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 3000, 0, 0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable(){
			@Override
			public void run()
			{
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
				ItemService.removeItemFromInventory(player, item);
				ItemService.addItems(player, Collections.singletonList(new QuestItems(182200008, 1)));
				qs.setQuestVarById(0, 2);
				updateQuestStatus(player, qs);
			}
		}, 3000);
		return true;
	}

	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId)
	{
		if(movieId != 151)
			return false;
		Player player = env.getPlayer();
		int instanceId = player.getInstanceId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs == null || qs.getStatus() != QuestStatus.START || qs.getQuestVars().getQuestVars() != 4)
			return false;
		QuestService.addNewSpawn(310010000, instanceId, 790001, (float) 220.6, (float) 247.8, (float) 206.0, (byte) 0, true);
		qs.setQuestVar(5);
		updateQuestStatus(player, qs);
		return true;
	}

	private boolean setPlayerClass(QuestEnv env, QuestState qs, PlayerClass playerClass)
	{
		Player player = env.getPlayer();
		player.getCommonData().setPlayerClass(playerClass);
		player.getCommonData().upgradePlayer();
		qs.setStatus(QuestStatus.REWARD);
		updateQuestStatus(player, qs);
		sendQuestDialog(player, env.getVisibleObject().getObjectId(), 5);
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
		if(var == 4 || (var >= 50 && var <= 55))
		{
			qs.setQuestVar(3);
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
			if(var == 4 || (var >= 50 && var <= 55) || var == 99)
			{
				if(player.getWorldId() != 310010000)
				{
					qs.setQuestVar(3);
					updateQuestStatus(player, qs);
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(SystemMessageId.QUEST_FAILED_$1, DataManager.QUEST_DATA.getQuestById(questId).getName()));
				}
				else
				{
					PacketSendUtility.sendPacket(player, new SM_ASCENSION_MORPH(1));
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onQuestFinishEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(qs != null && qs.getStatus() == QuestStatus.REWARD)
		{
			TeleportService.teleportTo(player, 210010000, 1, 242, 1638, 100, (byte) 20, 0);
			return true;
		}
		return false;
	}
}
