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
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SpellStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author Sarynth
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PulledEffect")
public class PulledEffect extends EffectTemplate
{
	@Override
	public void applyEffect(Effect effect)
	{
		effect.addToEffectedController(); 
	}

	@Override
	public void calculate(Effect effect)
	{
		if(effect.getEffector() instanceof Player && effect.getEffected() != null)
		{
			effect.addSucessEffect(this);
		}
		effect.setSpellStatus(SpellStatus.NONE);
	}

	@Override
	public void startEffect(final Effect effect)
	{
		final Creature effector = effect.getEffector();
		final Creature effected = effect.getEffected();
		effected.getEffectController().setAbnormal(EffectId.CANNOT_MOVE.getEffectId());
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run()
			{
				World.getInstance().updatePosition(
					effected,
					effector.getX(),
					effector.getY(),
					effector.getZ() + 0.25F,
					effected.getHeading());
				PacketSendUtility.broadcastPacketAndReceive(effected, new SM_FORCED_MOVE(effector, effected));
			}
		}, 1000);
	}

	@Override
	public void endEffect(final Effect effect)
	{
		effect.getEffected().getEffectController().unsetAbnormal(EffectId.CANNOT_MOVE.getEffectId());
	}
}
