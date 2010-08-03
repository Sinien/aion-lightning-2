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
package com.aionemu.gameserver.ai.state.handler;

import com.aionemu.gameserver.ai.AI;
import com.aionemu.gameserver.ai.desires.impl.AttackDesire;
import com.aionemu.gameserver.ai.desires.impl.MoveToTargetDesire;
import com.aionemu.gameserver.ai.desires.impl.SkillUseDesire;
import com.aionemu.gameserver.ai.state.AIState;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.gameobjects.stats.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LOOKATOBJECT;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 *
 */
public class AttackingStateHandler extends StateHandler
{
	@Override
	public AIState getState()
	{
		return AIState.ATTACKING;
	}

	/**
	 * State ATTACKING
	 * AI MonsterAi
	 * AI AggressiveAi
	 */
	@Override
	public void handleState(AIState state, AI<?> ai)
	{
		ai.clearDesires();

		Creature target = ((Npc)ai.getOwner()).getAggroList().getMostHated();
		if(target == null)
			return;

		Npc owner = (Npc) ai.getOwner();
		owner.setTarget(target);
		PacketSendUtility.broadcastPacket(owner, new SM_LOOKATOBJECT(owner));

		owner.setState(CreatureState.WEAPON_EQUIPPED);
		PacketSendUtility.broadcastPacket(owner,
			new SM_EMOTION(owner, EmotionType.START_EMOTE2, 0, target.getObjectId()));
		PacketSendUtility.broadcastPacket(owner,
			new SM_EMOTION(owner, EmotionType.ATTACKMODE, 0, target.getObjectId()));
		
		owner.getMoveController().setSpeed(owner.getGameStats().getCurrentStat(StatEnum.SPEED) / 1000f);
		owner.getMoveController().setDistance(owner.getGameStats().getCurrentStat(StatEnum.ATTACK_RANGE) / 1000f);
		
		if(owner.getNpcSkillList() != null)
			ai.addDesire(new SkillUseDesire(owner, AIState.USESKILL.getPriority()));
		ai.addDesire(new AttackDesire(owner, target, AIState.ATTACKING.getPriority()));
		if (owner.getGameStats().getCurrentStat(StatEnum.SPEED) != 0)
			ai.addDesire(new MoveToTargetDesire(owner, target, AIState.ATTACKING.getPriority()));

		ai.schedule();
	}
}
