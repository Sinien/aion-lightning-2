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
package com.aionemu.gameserver.model.templates.stats;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Luno
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "npc_stats_template")
public class NpcStatsTemplate extends StatsTemplate
{
	@XmlAttribute(name = "run_speed_fight")
	private float runSpeedFight;
	@XmlAttribute(name = "pdef")
	private int pdef;
	@XmlAttribute(name = "mdef")
	private int mdef;
	@XmlAttribute(name = "crit")
	private int crit;
	@XmlAttribute(name = "accuracy")
	private int accuracy;
	@XmlAttribute(name = "power")
	private int power;
	@XmlAttribute(name = "maxXp")
	private int maxXp;
	
	public float getRunSpeedFight()
	{
		return runSpeedFight;
	}

	/**
	 * @return the pdef
	 */
	public float getPdef()
	{
		return pdef;
	}

	/**
	 * @return the mdef
	 */
	public float getMdef()
	{
		return mdef;
	}

	/**
	 * @return the crit
	 */
	public float getCrit()
	{
		return crit;
	}

	/**
	 * @return the accuracy
	 */
	public float getAccuracy()
	{
		return accuracy;
	}

	/**
	 * @return the power
	 */
	public int getPower()
	{
		return power;
	}

	/**
	 * @return the maxXp
	 */
	public int getMaxXp()
	{
		return maxXp;
	}
	
}
