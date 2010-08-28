/*
 * This file is part of aion-lightning <aion-lightning.com>.
 *
 *  aion-lightning is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-lightning is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-lightning.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.movement.ActionObserver;
import com.aionemu.gameserver.controllers.movement.ActionObserver.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.stats.CreatureLifeStats;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.SkillType;

/**
 * @author ViAl
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MagicCounterAtkEffect")
public class MagicCounterAtkEffect extends EffectTemplate
{
	@XmlAttribute
	protected int	percent;
	@XmlAttribute
	protected int	maxdmg;
	
	@Override
	public void calculate(Effect effect)
	{
		if (calculateEffectResistRate(effect, null)) 
			effect.addSucessEffect(this);
	}
	
	@Override
	public void applyEffect(Effect effect)
	{
		effect.addToEffectedController();
	}
	
	@Override
	public void startEffect(final Effect effect)
	{
		final Creature effector = effect.getEffector();
		final Creature effected = effect.getEffected();
		final CreatureLifeStats<? extends Creature> cls = effect.getEffected().getLifeStats();
		ActionObserver observer = null;
		
		observer = new ActionObserver(ObserverType.SKILLUSE)
			{
				@Override
				public void skilluse(Skill skill)
				{
					if (skill.getSkillTemplate().getType()==SkillType.MAGICAL)
					{
						if (cls.getMaxHp()/100*percent<=maxdmg)
							effected.getController().onAttack(effector, effect.getSkillId(), TYPE.DAMAGE, cls.getMaxHp()/100*percent);
						else
							effected.getController().onAttack(effector, maxdmg);
					}
						
				}
			};

		effect.setActionObserver(observer, position);
		effected.getObserveController().addObserver(observer);
	}
	
	@Override
	public void endEffect(Effect effect)
	{
		ActionObserver observer = effect.getActionObserver(position);
		if (observer != null)
			effect.getEffected().getObserveController().removeObserver(observer);
	}
}
