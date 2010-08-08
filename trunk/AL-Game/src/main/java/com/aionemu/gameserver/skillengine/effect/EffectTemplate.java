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
package com.aionemu.gameserver.skillengine.effect;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.SkillElement;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.stats.StatEnum;
import com.aionemu.gameserver.skillengine.change.Change;
import com.aionemu.gameserver.skillengine.effect.modifier.ActionModifier;
import com.aionemu.gameserver.skillengine.effect.modifier.ActionModifiers;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.HopType;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.stats.StatFunctions;

/**
 * @author ATracer
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Effect")
public abstract class EffectTemplate 
{

	protected ActionModifiers modifiers;
    protected List<Change> change;
    @XmlAttribute
    protected int effectid;
	@XmlAttribute(required = true)
	protected int duration;
	@XmlAttribute(name = "randomtime")
	protected int randomTime;
	@XmlAttribute(name = "e")
	protected int position;
	@XmlAttribute(name = "basiclvl")
	protected int basicLvl;
	@XmlAttribute(name = "element")
	protected SkillElement element = SkillElement.NONE;
	@XmlElement(name = "subeffect")
	protected SubEffect subEffect;
	@XmlAttribute(name = "hoptype")
	protected HopType hopType;
	@XmlAttribute(name = "hopa")
	protected int hopA;
	@XmlAttribute(name = "hopb")
	protected int hopB;
	
	/**
	 * @return the duration
	 */
	public int getDuration()
	{
		return duration;
	}
	
	/**
	 * @return the randomtime
	 */
	public int getRandomTime()
	{
		return randomTime;
	}
	

	/**
	 * @return the modifiers
	 */
	public ActionModifiers getModifiers()
	{
		return modifiers;
	}


	/**
	 * @return the change
	 */
	public List<Change> getChange()
	{
		return change;
	}

	/**
	 * @return the effectid
	 */
	public int getEffectid()
	{
		return effectid;
	}

	/**
	 * @return the position
	 */
	public int getPosition()
	{
		return position;
	}

	/**
	 * @return the basicLvl
	 */
	public int getBasicLvl()
	{
		return basicLvl;
	}

	/**
	 * @return the element
	 */
	public SkillElement getElement()
	{
		return element;
	}


	/**
	 * @param value
	 * @return
	 */
	protected int applyActionModifiers(Effect effect, int value)
	{	
		if(modifiers == null)
			return value;
		
		/**
		 * Only one of modifiers will be applied now
		 */
		for(ActionModifier modifier : modifiers.getActionModifiers())
		{
			if(modifier.check(effect))
				return modifier.analyze(effect, value);
		}
		
		return value;
	}

	/**
	 *  Calculate effect result
	 *  
	 * @param effect
	 */
	public abstract void calculate(Effect effect);
	/**
	 *  Apply effect to effected 
	 *  
	 * @param effect
	 */
	public abstract void applyEffect(Effect effect);
	/**
	 *  Start effect on effected
	 *  
	 * @param effect
	 */
	public void startEffect(Effect effect){};
	
	/**
	 * 
	 * @param effect
	 */
	public void calculateSubEffect(Effect effect)
	{
		if(subEffect == null)
			return;
		
		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(subEffect.getSkillId());
		int duration = template.getEffectsDuration();
		Effect newEffect = new Effect(effect.getEffector(), effect.getEffected(), template, template.getLvl(), duration);
		newEffect.initialize();
		effect.setSpellStatus(newEffect.getSpellStatus());
		effect.setSubEffect(newEffect);
	}
	
	/**
	 *  Hate will be added to result value only if particular
	 *  effect template has success result
	 *  
	 * @param effect
	 */
	public void calculateHate(Effect effect)
	{	
		if(hopType == null)
			return;
		
		if(effect.getSuccessEffect().isEmpty())
			return;
		
		int currentHate = effect.getEffectHate();
		if(hopType != null)
		{
			switch(hopType)
			{
				case DAMAGE:
					currentHate += effect.getReserved1(); 
					break;
				case SKILLLV:
					int skillLvl = effect.getSkillLevel();
					currentHate += hopB + hopA * skillLvl; 
				default:
					break;
			}
		}
		if (currentHate == 0)
			currentHate = 1;
		effect.setEffectHate(StatFunctions.calculateHate(effect.getEffector(), currentHate));
	}
	
	/**
	 * 
	 * @param effect
	 */
	public void startSubEffect(Effect effect)
	{
		if(subEffect == null)
			return;
		
		effect.getSubEffect().applyEffect();
	}
	/**
	 *  Do periodic effect on effected
	 *  
	 * @param effect
	 */
	public void onPeriodicAction(Effect effect){};
	/**
	 *  End effect on effected
	 *  
	 * @param effect
	 */
	public void endEffect(Effect effect){};
	
	public boolean calculateEffectResistRate(Effect effect, StatEnum statEnum ) 
 	{ 
 		// TODO: Need correct value in client. 1000 = 100% 
		int effectPower = 1000; 
 		                 
 		//first resist? 
 		if (statEnum != null) 
 		{ 
			int stat = effect.getEffected().getGameStats().getCurrentStat(statEnum); 
 		    effectPower -= stat; 
 		} 
 		 
 		int attackerLevel = effect.getEffector().getLevel(); 
 		int targetLevel = effect.getEffected().getLevel(); 
 		                 
 		float multipler = 0.0f; 
	    int differ = (targetLevel - attackerLevel); 
	    //lvl mod 
 	    if(differ > 0 && differ < 8 ) 
	    { 
	        multipler = differ / 10f; 
 	        effectPower -= Math.round((effectPower * multipler)); 
        } 
        else if (differ >= 8) 
 	    { 
	        effectPower -= Math.round((effectPower * 0.80f)); 
	    } 
	    if (effect.getEffected() instanceof Npc) 
	    { 
	        float hpGaugeMod = ((Npc) effect.getEffected()).getObjectTemplate().getHpGauge(); 
	        effectPower -= (200*(1+(hpGaugeMod/10))); 
	    } 
	    return  (Rnd.get()*1000 < effectPower); 
	} 
}
