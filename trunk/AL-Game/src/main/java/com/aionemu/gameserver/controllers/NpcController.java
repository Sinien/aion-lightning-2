/*
 * This file is part of aion-emu <aion-emu.com>.
 *
 *  aion-emu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-emu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-emu.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.controllers;

import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.aionemu.gameserver.ai.AI;
import com.aionemu.gameserver.ai.events.Event;
import com.aionemu.gameserver.ai.npcai.DummyAi;
import com.aionemu.gameserver.controllers.attack.AttackResult;
import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.gameobjects.stats.NpcGameStats;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LOOKATOBJECT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SELL_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TRADELIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.restrictions.RestrictionsManager;
import com.aionemu.gameserver.services.CraftSkillUpdateService;
import com.aionemu.gameserver.services.CubeExpandService;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.services.TeleportService;
import com.aionemu.gameserver.services.TradeService;
import com.aionemu.gameserver.services.WarehouseService;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * This class is for controlling Npc's
 * 
 * @author -Nemesiss-, ATracer (2009-09-29), Sarynth
 */
public class NpcController extends CreatureController<Npc>
{
	private static final Logger	log	= Logger.getLogger(NpcController.class);

	@Override
	public void notSee(VisibleObject object, boolean isOutOfRange)
	{
		super.notSee(object, isOutOfRange);
		if(object instanceof Player || object instanceof Summon)
			getOwner().getAi().handleEvent(Event.NOT_SEE_PLAYER);			
	}

	@Override
	public void see(VisibleObject object)
	{
		super.see(object);
		Npc owner = getOwner();
		owner.getAi().handleEvent(Event.SEE_CREATURE);
		if(object instanceof Player)
		{
			owner.getAi().handleEvent(Event.SEE_PLAYER);
			//TODO check on retail how walking npc is presented, probably need replace emotion
			// with some state etc.
			if(owner.getMoveController().isWalking())
				PacketSendUtility.sendPacket((Player) object, new SM_EMOTION(owner, EmotionType.WALK));
		}	
		else if(object instanceof Summon)
		{
			owner.getAi().handleEvent(Event.SEE_PLAYER);
		}
	}

	@Override
	public void onRespawn()
	{
		super.onRespawn();
		
		cancelTask(TaskId.DECAY);
		Npc owner = getOwner();
		
		//set state from npc templates
		if(owner.getObjectTemplate().getState() != 0)
			owner.setState(owner.getObjectTemplate().getState());
		else
			owner.setState(CreatureState.NPC_IDLE);
		
		owner.getLifeStats().setCurrentHpPercent(100);
		owner.getAi().handleEvent(Event.RESPAWNED);
		if(owner.getSpawn().getNpcFlyState() != 0)
		{
			owner.setState(CreatureState.FLYING);
		}
	}

	public void onDespawn(boolean forced)
	{
		if(forced)
			cancelTask(TaskId.DECAY);

		Npc owner = getOwner();
		if(owner == null || !owner.isSpawned())
			return;

		owner.getAi().handleEvent(Event.DESPAWN);
		World.getInstance().despawn(owner);
	}

	@Override
	public void onDie(Creature lastAttacker)
	{
		super.onDie(lastAttacker);
		Npc owner = getOwner();

		addTask(TaskId.DECAY, RespawnService.scheduleDecayTask(this.getOwner()));
		scheduleRespawn();

		PacketSendUtility.broadcastPacket(owner,
			new SM_EMOTION(owner, EmotionType.DIE, 0, lastAttacker == null ? 0 : lastAttacker.getObjectId()));
		
		// Monster Controller overrides this method.
		this.doReward();

		owner.getAi().handleEvent(Event.DIED);

		// deselect target at the end
		owner.setTarget(null);
		PacketSendUtility.broadcastPacket(owner, new SM_LOOKATOBJECT(owner));
	}

	@Override
	public Npc getOwner()
	{
		return (Npc) super.getOwner();
	}

	@Override
	public void onDialogRequest(Player player)
	{
		getOwner().getAi().handleEvent(Event.TALK);
		
		if(QuestEngine.getInstance().onDialog(new QuestEnv(getOwner(), player, 0, -1)))
			return;
		// TODO need check here
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getOwner().getObjectId(), 10));
	}

	/**
	 * This method should be called to make forced despawn of NPC and delete it from the world
	 */
	public void onDelete()
	{
		if(getOwner().isInWorld())
		{
			this.getOwner().getAi().clearDesires();
			this.onDespawn(true);
			this.delete();
		}
	}

	/**
	 * Handle dialog
	 */
	@Override
	public void onDialogSelect(int dialogId, final Player player, int questId)
	{

		Npc npc = getOwner();
		if (!MathUtil.isInRange(npc, player, 10))
			return;
		int targetObjectId = npc.getObjectId();

		if(QuestEngine.getInstance().onDialog(new QuestEnv(npc, player, questId, dialogId)))
			return;
		switch(dialogId)
		{
			case 2:
				int tradeModifier = DataManager.TRADE_LIST_DATA.getTradeListTemplate(npc.getNpcId()).getSellPriceRate();
				PacketSendUtility.sendPacket(player, new SM_TRADELIST(npc,
					TradeService.getTradeListData().getTradeListTemplate(npc.getNpcId()),
					player.getPrices().getVendorBuyModifier()*tradeModifier/100));
				break;
			case 3:
				PacketSendUtility.sendPacket(player, new SM_SELL_ITEM(targetObjectId, player.getPrices().getVendorSellModifier()));
				break;
			case 4:
				// stigma
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(targetObjectId, 1));		
				break;
			case 5:
				// create legion
				if(MathUtil.isInRange(npc, player, 10)) // avoiding exploit with sending fake dialog_select packet
				{
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(targetObjectId, 2));
				}
				else
				{
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.LEGION_CREATE_TOO_FAR_FROM_NPC());
				}
				break;
			case 6:
				// disband legion
				if(MathUtil.isInRange(npc, player, 10)) // avoiding exploit with sending fake dialog_select packet
				{
					LegionService.getInstance().requestDisbandLegion(npc, player);
				}
				else
				{
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.LEGION_DISPERSE_TOO_FAR_FROM_NPC());
				}
				break;
			case 7:
				// recreate legion
				if(MathUtil.isInRange(npc, player, 10)) // voiding exploit with sending fake client dialog_select
				// packet
				{
					LegionService.getInstance().recreateLegion(npc, player);
				}
				else
				{
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.LEGION_DISPERSE_TOO_FAR_FROM_NPC());
				}
				break;
			case 20:
				// warehouse
				if(MathUtil.isInRange(npc, player, 10)) // voiding exploit with sending fake client dialog_select
				// packet
				{
					if(!RestrictionsManager.canUseWarehouse(player))
						return;

					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(targetObjectId, 26));
					WarehouseService.sendWarehouseInfo(player, true);
				}
				break;
			case 27:
				// Consign trade?? npc karinerk, koorunerk
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(targetObjectId, 13));
				break;
			case 29:
				// soul healing
				final long expLost = player.getCommonData().getExpRecoverable();
				final double factor = (expLost < 1000000 ?
					0.25 - (0.00000015 * expLost) 
					: 0.1);
				final int price = (int) (expLost * factor);
				
				RequestResponseHandler responseHandler = new RequestResponseHandler(npc){
					@Override
					public void acceptRequest(Creature requester, Player responder)
					{
						if(ItemService.decreaseKinah(player, price))
						{
							PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.EXP(String.valueOf(expLost)));
							PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.SOUL_HEALED());
							player.getCommonData().resetRecoverableExp();
						}
						else
						{
							PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.NOT_ENOUGH_KINAH(price));
						}
					}

					@Override
					public void denyRequest(Creature requester, Player responder)
					{
						// no message
					}
				};
				if(player.getCommonData().getExpRecoverable() > 0)
				{
					boolean result = player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_SOUL_HEALING,
						responseHandler);
					if(result)
					{
						PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(
							SM_QUESTION_WINDOW.STR_SOUL_HEALING, 0, String.valueOf(price)
						));
					}
				}
				else
				{
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.DONT_HAVE_RECOVERED_EXP());
				}
				break;
			case 30:
				switch(npc.getNpcId())
				{
					case 204089:
						TeleportService.teleportTo(player, 120010000, 1, 984f, 1543f, 222.1f, 0);
						break;
					case 203764:
						TeleportService.teleportTo(player, 110010000, 1, 1462.5f, 1326.1f, 564.1f, 0);
						break;
					case 203981:
						TeleportService.teleportTo(player, 210020000, 1, 439.3f, 422.2f, 274.3f, 0);
						break;
				}
				break;
			case 31:
				switch(npc.getNpcId())
				{
					case 204087:
						TeleportService.teleportTo(player, 120010000, 1, 1005.1f, 1528.9f, 222.1f, 0);
						break;
					case 203875:
						TeleportService.teleportTo(player, 110010000, 1, 1470.3f, 1343.5f, 563.7f, 21);
						break;
					case 203982:
						TeleportService.teleportTo(player, 210020000, 1, 446.2f, 431.1f, 274.5f, 0);
						break;
				}			
				break;				
			case 35:
				// Godstone socketing
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(targetObjectId, 21));
				break;
			case 36:
				// remove mana stone
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(targetObjectId, 20));
				break;
			case 37:
				// modify appearance
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(targetObjectId, 19));
				break;
			case 38:
				// flight and teleport
				TeleportService.showMap(player, targetObjectId, npc.getNpcId());
				break;
			case 39:
				// improve extraction
			case 40:
				// learn tailoring armor smithing etc...
				CraftSkillUpdateService.getInstance().learnSkill(player, npc);
				break;
			case 41:
				// expand cube
				CubeExpandService.expandCube(player, npc);
				break;
			case 42:
				WarehouseService.expandWarehouse(player, npc);
				break;
			case 47:
				// legion warehouse
				if(MathUtil.isInRange(npc, player, 10))
					LegionService.getInstance().openLegionWarehouse(player);
				break;
			case 50:
				// WTF??? Quest dialog packet
				break;
			case 52:
				if(MathUtil.isInRange(npc, player, 10)) // avoiding exploit with sending fake dialog_select packet
				{
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(targetObjectId, 28));
				}
				break;
			case 53:
				// coin reward
				PacketSendUtility.sendPacket(player, new SM_MESSAGE(0, null, "This feature is not available yet",
					ChatType.ANNOUNCEMENTS));
				break;
			case 60:
				// armsfusion
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(targetObjectId, 29));
				break;
			case 61:
				// armsbreaking
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(targetObjectId, 30));
				break;
			default:
				if(questId > 0)
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(targetObjectId, dialogId, questId));
				else
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(targetObjectId, dialogId));
				break;
		}
	}

	@Override
	public void onAttack(Creature creature, int skillId, TYPE type, int damage)
	{
		if(getOwner().getLifeStats().isAlreadyDead())
			return;

		super.onAttack(creature, skillId, type, damage);
		
		Npc npc = getOwner();
		
		Creature actingCreature = creature.getActingCreature();
		if(actingCreature instanceof Player)
			if(QuestEngine.getInstance().onAttack(new QuestEnv(npc, (Player) actingCreature, 0, 0)))
				return;

		AI<?> ai = npc.getAi();
		if(ai instanceof DummyAi)
		{
			log.warn("CHECKPOINT: npc attacked without ai " + npc.getObjectTemplate().getTemplateId());
			return;
		}
		for (VisibleObject obj : this.getOwner().getKnownList())
		{
			if (obj instanceof Npc)
			{
				Npc tmp = (Npc)obj;
				if (getOwner().isSupportFrom(tmp) && MathUtil.isInRange(getOwner(), obj, 10))
				{
					tmp.getAggroList().addHate(creature, 10);
				}
					
			}
		}
		npc.getLifeStats().reduceHp(damage, actingCreature);

		PacketSendUtility.broadcastPacket(npc, new SM_ATTACK_STATUS(npc, type, skillId, damage));
	}

	@Override
	public void attackTarget(Creature target)
	{
		Npc npc = getOwner();
		
		/**
		 * Check all prerequisites
		 */
		if(npc == null || npc.getLifeStats().isAlreadyDead() || !npc.isSpawned())
			return;

		if(!npc.canAttack())
			return;

		AI<?> ai = npc.getAi();
		NpcGameStats gameStats = npc.getGameStats();

		if(target == null || target.getLifeStats().isAlreadyDead())
		{
			ai.handleEvent(Event.MOST_HATED_CHANGED);
			return;
		}
		
		/**
		 * notify attack observers
		 */
		super.attackTarget(target);
		
		/**
		 * Calculate and apply damage
		 */
		List<AttackResult> attackList = AttackUtil.calculateAttackResult(npc, target);

		int damage = 0;
		for(AttackResult result : attackList)
		{
			damage += result.getDamage();
		}

		int attackType = 0; // TODO investigate attack types (0 or 1)
		PacketSendUtility.broadcastPacket(npc, new SM_ATTACK(npc, target, gameStats
			.getAttackCounter(), 274, attackType, attackList));
		
		target.getController().onAttack(npc, damage);
		gameStats.increaseAttackCounter();
	}

	/**
	 * Schedule respawn of npc
	 * In instances - no npc respawn
	 */
	public void scheduleRespawn()
	{	
		if(getOwner().isInInstance())
			return;
		
		int instanceId = getOwner().getInstanceId();
		if(!getOwner().getSpawn().isNoRespawn(instanceId))
		{
			Future<?> respawnTask = RespawnService.scheduleRespawnTask(getOwner());
			addTask(TaskId.RESPAWN, respawnTask);
		}
	}
}