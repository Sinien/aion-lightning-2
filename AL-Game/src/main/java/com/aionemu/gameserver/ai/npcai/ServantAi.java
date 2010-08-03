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
package com.aionemu.gameserver.ai.npcai;

import com.aionemu.gameserver.ai.AI;
import com.aionemu.gameserver.ai.desires.AbstractDesire;
import com.aionemu.gameserver.ai.events.Event;
import com.aionemu.gameserver.ai.events.handler.EventHandler;
import com.aionemu.gameserver.ai.state.AIState;
import com.aionemu.gameserver.ai.state.handler.NoneNpcStateHandler;
import com.aionemu.gameserver.ai.state.handler.StateHandler;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Servant;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author ATracer
 *
 */
public class ServantAi extends NpcAi
{
	public ServantAi()
	{
		/**
		 * Event handlers
		 */
		this.addEventHandler(new RespawnEventHandler());
		
		/**
		 * State handlers
		 */
		this.addStateHandler(new ActiveServantStateHandler());
		this.addStateHandler(new NoneNpcStateHandler());
	}
	
	public class RespawnEventHandler implements EventHandler
	{
		@Override
		public Event getEvent()
		{
			return Event.RESPAWNED;
		}

		@Override
		public void handleEvent(Event event, AI<?> ai)
		{
			ai.setAiState(AIState.ACTIVE);
			if(!ai.isScheduled())
				ai.analyzeState();
		}

	}
	
	class ActiveServantStateHandler extends StateHandler
	{
		@Override
		public AIState getState()
		{
			return AIState.ACTIVE;
		}

		@Override
		public void handleState(AIState state, AI<?> ai)
		{
			ai.clearDesires();
			Servant owner = (Servant) ai.getOwner();
			Creature servantOwner = owner.getCreator();

			VisibleObject servantOwnerTarget = servantOwner.getTarget();
			if(servantOwnerTarget instanceof Creature)
			{
				ai.addDesire(new ServantSkillUseDesire(owner, (Creature) servantOwnerTarget, AIState.ACTIVE
					.getPriority()));
			}

			if(ai.desireQueueSize() == 0)
				ai.handleEvent(Event.NOTHING_TODO);
			else
				ai.schedule();
		}
	}
	
	class ServantSkillUseDesire extends AbstractDesire
	{
		/**
		 * Trap object
		 */
		private Servant		owner;
		/**
		 * Owner of trap
		 */
		private Creature	target;

		/**
		 * 
		 * @param desirePower
		 * @param owner
		 */
		private ServantSkillUseDesire(Servant owner, Creature target, int desirePower)
		{
			super(desirePower);
			this.owner = owner;
			this.target = target;
		}

		@Override
		public boolean handleDesire(AI<?> ai)
		{		
			if(target == null || target.getLifeStats().isAlreadyDead())
				return true;
			
			if(!owner.getActingCreature().isEnemy(target))
				return false;
			
			Skill skill = SkillEngine.getInstance().getSkill(owner, owner.getSkillId(), 1, target);
			if(skill != null)
			{
				skill.useSkill();
				
				//TODO add to skills cost paramater HP
				int maxHp = owner.getLifeStats().getMaxHp();
				owner.getLifeStats().reduceHp(Math.round(maxHp / 3f), null);
			}
			return true;
		}

		@Override
		public int getExecutionInterval()
		{
			return 10;//TODO unhardcode
		}

		@Override
		public void onClear()
		{
			// TODO Auto-generated method stub
		}
	}
}
