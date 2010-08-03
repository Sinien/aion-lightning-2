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
package com.aionemu.gameserver.utils.rates;

/**
 * @author ATracer
 */
public abstract class Rates
{
	public abstract int getGroupXpRate();

	public abstract int getXpRate();

	public abstract float getApNpcRate();

	public abstract float getApPlayerRate();

	public abstract float getGatheringXPRate();
	
	public abstract float getCraftingXPRate();
	
	public abstract int getDropRate();

	public abstract int getQuestXpRate();

	public abstract int getQuestKinahRate();

	/**
	 * @param membership
	 * @return Rates
	 */
	public static Rates getRatesFor(byte membership)
	{
		switch(membership)
		{
			case 0:
				return new RegularRates();
			case 1:
				return new PremiumRates();
			default:
				return new RegularRates();
		}
	}
}
