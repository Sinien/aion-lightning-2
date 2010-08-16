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
package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TRANSFORM;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Sweetkr
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransformEffect")
public class TransformEffect extends EffectTemplate
{
	@XmlAttribute
	protected int model;
	
	@Override
	public void applyEffect(Effect effect)
	{
		final Creature effected = effect.getEffected();
		boolean transformed = false;
		if(effected instanceof Npc)
		{
			transformed = effected.getTransformedModelId() == effected.getObjectTemplate().getTemplateId();
		}
		else if(effected instanceof Player)
		{
			transformed = effected.getTransformedModelId() != 0;
		}
		if (transformed)
		{
			for ( Effect tmp : effected.getEffectController().getAbnormalEffects())
			{
				if (effect.getSkillId() == tmp.getSkillId())
					continue;
				boolean abort = false;
				for (EffectTemplate template : tmp.getEffectTemplates())
				{
					if (template instanceof TransformEffect)
					{
						abort = true;
						break;
					}
				}
				if (abort)
					tmp.endEffect();
			}
		}
		effect.addToEffectedController();
	}

	@Override
	public void calculate(Effect effect)
	{
		//TODO calc probability
		effect.addSucessEffect(this);
	}

	@Override
	public void endEffect(Effect effect)
	{
		final Creature effected = effect.getEffected();
		effected.getEffectController().unsetAbnormal(EffectId.SHAPECHANGE.getEffectId());

		if(effected instanceof Npc)
		{
			effected.setTransformedModelId(effected.getObjectTemplate().getTemplateId());
		}
		else if(effected instanceof Player)
		{
			effected.setTransformedModelId(0);
		}
		PacketSendUtility.broadcastPacketAndReceive(effected, new SM_TRANSFORM(effected));
	}

	@Override
	public void startEffect(final Effect effect)
	{
		final Creature effected = effect.getEffected();
		switch(effect.getSkillId())	//check, if allowed fly transform - don't ban flying
		{
			case (689):		//MAU transform effect
			case (690):
			case (780):
			case (781):
			case (782):
			case (789):
			case (790):
			case (791):
			case (9737):	//Abyss trasfrom effect
			case (9738):
			case (9739):
			case (9740):
			case (9741):
			case (9742):
			case (9743):
			case (9744):
			case (9745):
			case (9746):
				break;
			default:
				effected.getEffectController().setAbnormal(EffectId.SHAPECHANGE.getEffectId());
			}
		effected.setTransformedModelId(model);
		PacketSendUtility.broadcastPacketAndReceive(effected, new SM_TRANSFORM(effected));
	}
}
