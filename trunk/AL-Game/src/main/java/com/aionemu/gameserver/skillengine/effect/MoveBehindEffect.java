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
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_MOVE;
import com.aionemu.gameserver.skillengine.action.DamageType;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author Sarynth
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MoveBehindEffect")
public class MoveBehindEffect extends DamageEffect
{
	@Override
	public void applyEffect(Effect effect)
	{
		super.applyEffect(effect);
		final Player effector = (Player)effect.getEffector();
		final Creature effected = effect.getEffected();
		
		// Move Effector to Effected
		double radian = Math.toRadians(MathUtil.convertHeadingToDegree(effected.getHeading()));
		float x1 = (float)(Math.cos(Math.PI + radian) * 1.3F);
		float y1 = (float)(Math.sin(Math.PI + radian) * 1.3F);
		World.getInstance().updatePosition(
			effector,
			effected.getX() + x1,
			effected.getY() + y1,
			effected.getZ() + 0.25F,
			effected.getHeading());
		
		PacketSendUtility.sendPacket(effector,
			new SM_PLAYER_MOVE(
				effector.getX(),
				effector.getY(),
				effector.getZ(),
				effector.getHeading()
			)
		);
	}
	
	@Override
	public void calculate(Effect effect)
	{
		if(effect.getEffector() instanceof Player && effect.getEffected() != null)
		{
			super.calculate(effect, DamageType.PHYSICAL);
		}
	}
	
}
