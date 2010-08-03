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

import java.util.concurrent.Future;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.stats.StatEnum;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MpUseOverTimeEffect")
public class MpUseOverTimeEffect extends EffectTemplate
{
	@XmlAttribute(required = true)
	protected int	checktime;
	@XmlAttribute
	protected int	value;

	@Override
	public void applyEffect(final Effect effect)
	{
		Creature effected = effect.getEffected();
		int maxMp = effected.getGameStats().getCurrentStat(StatEnum.MAXMP);
		final int requiredMp = maxMp * value / 100;

		Future<?> task = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Runnable(){

			@Override
			public void run()
			{
				onPeriodicAction(effect, requiredMp);
			}
		}, 0, checktime);
		effect.setMpUseTask(task);
	}

	public void onPeriodicAction(Effect effect, int value)
	{
		Creature effected = effect.getEffected();
		if(effected.getLifeStats().getCurrentMp() < value)
			effect.endEffect();

		effected.getLifeStats().reduceMp(value);
	}

	@Override
	public void calculate(Effect effect)
	{
		Creature effected = effect.getEffected();
		int maxMp = effected.getGameStats().getCurrentStat(StatEnum.MAXMP);
		int requiredMp = maxMp * value / 100;
		if(effected.getLifeStats().getCurrentMp() < requiredMp)
			return;

		effect.addSucessEffect(this);
	}

}
