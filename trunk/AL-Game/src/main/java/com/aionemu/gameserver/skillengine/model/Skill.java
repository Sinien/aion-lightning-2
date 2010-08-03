/*
 * This file is part of aion-unique <aion-unique.com>.
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
package com.aionemu.gameserver.skillengine.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.movement.StartMovingListener;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.stats.StatEnum;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CASTSPELL;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CASTSPELL_END;
import com.aionemu.gameserver.restrictions.RestrictionsManager;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.action.Action;
import com.aionemu.gameserver.skillengine.action.Actions;
import com.aionemu.gameserver.skillengine.condition.Condition;
import com.aionemu.gameserver.skillengine.condition.Conditions;
import com.aionemu.gameserver.skillengine.effect.EffectId;
import com.aionemu.gameserver.skillengine.properties.Properties;
import com.aionemu.gameserver.skillengine.properties.Property;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public class Skill
{
	private List<Creature> effectedList;
	
	private Creature firstTarget;
	
	private Creature effector;
	
	private int skillLevel;
	
	private int skillStackLvl;
	
	private StartMovingListener conditionChangeListener;
	
	private SkillTemplate skillTemplate;

	private boolean	firstTargetRangeCheck = true;
	
	private ItemTemplate itemTemplate;
	
	private int	targetType;
	
	private boolean chainSuccess = true;
	
	private float x;
	private float y;
	private float z;
	
	/**
	 * Duration that depends on BOOST_CASTING_TIME
	 */
	private int duration;
	
	public enum SkillType
	{
		CAST,
		ITEM,
		PASSIVE
	}
	
	/**
	 *  Each skill is a separate object upon invocation
	 *  Skill level will be populated from player SkillList
	 *  
	 * @param skillTemplate
	 * @param effector
	 * @param world
	 */
	public Skill(SkillTemplate skillTemplate, Player effector, Creature firstTarget)
	{
		this(skillTemplate, effector,
			effector.getSkillList().getSkillLevel(skillTemplate.getSkillId()), firstTarget);
	}	

	/**
	 * 
	 * @param skillTemplate
	 * @param effector
	 * @param skillLvl
	 * @param firstTarget
	 */
	public Skill(SkillTemplate skillTemplate, Creature effector, int skillLvl, Creature firstTarget) {
		this.effectedList = new ArrayList<Creature>();
		this.conditionChangeListener = new StartMovingListener();
		this.firstTarget = firstTarget;
		this.skillLevel = skillLvl;
		this.skillStackLvl = skillTemplate.getLvl();
		this.skillTemplate = skillTemplate;
		this.effector = effector;
	}

	/**
	 * Check if the skill can be used
	 * 
	 * @return True if the skill can be used
	 */
	public boolean canUseSkill()
	{
		
		if(!setProperties(skillTemplate.getInitproperties()))
			return false;
		
		if(!preCastCheck())
			return false;
		
		if(!setProperties(skillTemplate.getSetproperties()))
			return false;
		
		
		effector.setCasting(this);
		Iterator<Creature> effectedIter = effectedList.iterator();
		while(effectedIter.hasNext())
		{
			Creature effected = effectedIter.next();
			if(effected == null)
				effected = effector;

			if(effector instanceof Player)
			{
				if (!RestrictionsManager.canAffectBySkill((Player)effector, effected))
					effectedIter.remove();
			}
			else
			{
				if(effector.getEffectController().isAbnormalState(EffectId.CANT_ATTACK_STATE))					
					effectedIter.remove();
			}
		}
		effector.setCasting(null);
		
		// TODO: Enable non-targeted, non-point AOE skills to trigger. 
		if(targetType == 0 && effectedList.size() == 0)
		{
			return false;
		}
		return true;
	}

	/**
	 *  Skill entry point
	 */
	public void useSkill()
	{
		if (!canUseSkill())
			return;
		
		effector.getObserveController().notifySkilluseObservers(this);
		
		//start casting
		effector.setCasting(this);
		
		int skillDuration = skillTemplate.getDuration();
		int currentStat = effector.getGameStats().getCurrentStat(StatEnum.BOOST_CASTING_TIME);
		this.duration = skillDuration + Math.round(skillDuration * (100 - currentStat) / 100f);

		int cooldown = skillTemplate.getCooldown();
		if(cooldown != 0)
			effector.setSkillCoolDown(skillTemplate.getSkillId(), cooldown * 100 + this.duration + System.currentTimeMillis());
		
		if(duration < 0)
			duration = 0;
		
		if(skillTemplate.isActive() || skillTemplate.isToggle())
		{
			startCast();
		}
		
		effector.getObserveController().attach(conditionChangeListener);
		
		if(this.duration > 0)
		{
			schedule(this.duration);
		}
		else
		{
			endCast();
		}
	}
	
	/**
	 * Penalty success skill
	 */
	private void startPenaltySkill()
	{
		if(skillTemplate.getPenaltySkillId() == 0)
			return;
		
		Skill skill = SkillEngine.getInstance().getSkill(effector, skillTemplate.getPenaltySkillId(), 1, firstTarget);
		skill.useSkill();
	}
	
	/**
	 *  Start casting of skill
	 */
	private void startCast()
	{
		int targetObjId = firstTarget != null ? firstTarget.getObjectId() : 0;
		
		switch(targetType)
		{
			case 0: // PlayerObjectId as Target
				PacketSendUtility.broadcastPacketAndReceive(effector,
					new SM_CASTSPELL(
						effector.getObjectId(),
						skillTemplate.getSkillId(),
						skillLevel,
						targetType,
						targetObjId,
						this.duration));
				break;
				
			case 1: // XYZ as Target
				PacketSendUtility.broadcastPacketAndReceive(effector,
					new SM_CASTSPELL(
						effector.getObjectId(),
						skillTemplate.getSkillId(),
						skillLevel,
						targetType,
						x, y, z,
						this.duration));
				break;
		}
	}
	
	/**
	 *  Apply effects and perform actions specified in skill template
	 */
	private void endCast()
	{
		if(!effector.isCasting())
			return;

		//stop casting must be before preUsageCheck()
		effector.setCasting(null);
		
		if(!preUsageCheck())
			return;

		/**
		 * Create effects and precalculate result
		 */
		int spellStatus = 0;
		
		List<Effect> effects = new ArrayList<Effect>();		 
		if(skillTemplate.getEffects() != null)
		{
			for(Creature effected : effectedList)
			{
				Effect effect = new Effect(effector, effected, skillTemplate, skillLevel, 0, itemTemplate);
				effect.initialize();
				spellStatus = effect.getSpellStatus().getId();
				effects.add(effect);
			}
		}
		
		// Check Chain Skill Result
		int chainProb = skillTemplate.getChainSkillProb();
		if (chainProb != 0)
		{
			if (Rnd.get(100) < chainProb)
				this.chainSuccess = true;
			else
				this.chainSuccess = false;
		}
		
		/**
		 * If castspell - send SM_CASTSPELL_END packet
		 */
		if(skillTemplate.isActive() || skillTemplate.isToggle())
		{
			sendCastspellEnd(spellStatus, effects);
		}
		
		/**
		 * Perform necessary actions (use mp,dp items etc)
		 */
		Actions skillActions = skillTemplate.getActions();
		if(skillActions != null)
		{
			for(Action action : skillActions.getActions())
			{	
				action.act(this);
			}
		}
		
		/**
		 * Apply effects to effected objects
		 */
		for(Effect effect : effects)
		{
			effect.applyEffect();
		}
		
		/**
		 * Use penalty skill (now 100% success)
		 */
		startPenaltySkill();
	}

	/**
	 * @param spellStatus
	 * @param effects
	 */
	private void sendCastspellEnd(int spellStatus, List<Effect> effects)
	{
		switch(targetType)
		{
			case 0: // PlayerObjectId as Target
				PacketSendUtility.broadcastPacketAndReceive(effector,
					new SM_CASTSPELL_END(
						effector,
						firstTarget, // Need all targets...
						effects,
						skillTemplate.getSkillId(),
						skillLevel,
						skillTemplate.getCooldown(),
						chainSuccess,
						spellStatus));
				break;
				
			case 1: // XYZ as Target
				PacketSendUtility.broadcastPacketAndReceive(effector,
					new SM_CASTSPELL_END(
						effector,
						firstTarget, // Need all targets...
						effects,
						skillTemplate.getSkillId(),
						skillLevel,
						skillTemplate.getCooldown(),
						chainSuccess,
						spellStatus, x, y, z));
				break;
		}
	}
	/**
	 *  Schedule actions/effects of skill (channeled skills)
	 */
	private void schedule(int delay)
	{
		ThreadPoolManager.getInstance().schedule(new Runnable() 
		{
			public void run() 
			{
				endCast();
			}   
		}, delay);
	}
	
	/**
	 *  Check all conditions before starting cast
	 */
	private boolean preCastCheck()
	{
		Conditions skillConditions = skillTemplate.getStartconditions();
		return checkConditions(skillConditions);
	}
	
	/**
	 *  Check all conditions before using skill
	 */
	private boolean preUsageCheck()
	{
		Conditions skillConditions = skillTemplate.getUseconditions();
		return checkConditions(skillConditions);
	}
	
	private boolean checkConditions(Conditions conditions)
	{
		if(conditions != null)
		{
			for(Condition condition : conditions.getConditions())
			{
				if(!condition.verify(this))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean setProperties(Properties properties)
	{
		if(properties != null)
		{
			for(Property property : properties.getProperties())
			{
				if(!property.set(this))
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @return the effectedList
	 */
	public List<Creature> getEffectedList()
	{
		return effectedList;
	}

	/**
	 * @return the effector
	 */
	public Creature getEffector()
	{
		return effector;
	}

	/**
	 * @return the skillLevel
	 */
	public int getSkillLevel()
	{
		return skillLevel;
	}

	/**
	 * @return the skillStackLvl
	 */
	public int getSkillStackLvl()
	{
		return skillStackLvl;
	}

	/**
	 * @return the conditionChangeListener
	 */
	public StartMovingListener getConditionChangeListener()
	{
		return conditionChangeListener;
	}

	/**
	 * @return the skillTemplate
	 */
	public SkillTemplate getSkillTemplate()
	{
		return skillTemplate;
	}

	/**
	 * @return the firstTarget
	 */
	public Creature getFirstTarget()
	{
		return firstTarget;
	}

	/**
	 * @param firstTarget the firstTarget to set
	 */
	public void setFirstTarget(Creature firstTarget)
	{
		this.firstTarget = firstTarget;
	}

	/**
	 * @return true or false
	 */
	public boolean isPassive()
	{
		return skillTemplate.getActivationAttribute() == ActivationAttribute.PASSIVE;
	}

	/**
	 * @return the firstTargetRangeCheck
	 */
	public boolean isFirstTargetRangeCheck()
	{
		return firstTargetRangeCheck;
	}

	/**
	 * @param firstTargetRangeCheck the firstTargetRangeCheck to set
	 */
	public void setFirstTargetRangeCheck(boolean firstTargetRangeCheck)
	{
		this.firstTargetRangeCheck = firstTargetRangeCheck;
	}

	/**
	 * @param itemTemplate the itemTemplate to set
	 */
	public void setItemTemplate(ItemTemplate itemTemplate)
	{
		this.itemTemplate = itemTemplate;
	}
	
	/**
	 * @param targetType
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setTargetType(int targetType, float x, float y, float z)
	{
		this.targetType = targetType;
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
