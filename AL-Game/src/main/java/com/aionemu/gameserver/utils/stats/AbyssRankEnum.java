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
package com.aionemu.gameserver.utils.stats;
/**
 * @author ATracer
 * @author Sarynth
 */
public enum AbyssRankEnum
{
	GRADE9_SOLDIER(1, 120, 24, 0),
	GRADE8_SOLDIER(2, 168, 37, 1200),
	GRADE7_SOLDIER(3, 235, 58, 4220),
	GRADE6_SOLDIER(4, 329, 91, 10990),
	GRADE5_SOLDIER(5, 461, 143, 23500),
	GRADE4_SOLDIER(6, 645, 225, 42780),
	GRADE3_SOLDIER(7, 903, 356, 69700),
	GRADE2_SOLDIER(8, 1264, 561, 105600),
	GRADE1_SOLDIER(9, 1770, 885, 150800),
	STAR1_OFFICER(10, 2124, 1195, 214100),
	STAR2_OFFICER(11, 2549, 1616, 278700),
	STAR3_OFFICER(12, 3059, 2184, 344500),
	STAR4_OFFICER(13, 3671, 2949, 411700),
	STAR5_OFFICER(14, 4405, 3981, 488200),
	GENERAL(15, 5286, 5374, 565400),
	GREAT_GENERAL(16, 6343, 7258, 643200),
	COMMANDER(17, 7612, 9799, 721600),
	SUPREME_COMMANDER(18, 9134, 13229, 800700);
	
	private int id;
	private int pointsGained;
	private int pointsLost;		
	private int required;
	
	private AbyssRankEnum(int id, int pointsGained, int pointsLost, int required)
	{
		this.id = id;
		this.pointsGained = pointsGained;
		this.pointsLost = pointsLost;
		this.required = required;
	}

	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @return the pointsLost
	 */
	public int getPointsLost()
	{
		return pointsLost;
	}

	/**
	 * @return the pointsGained
	 */
	public int getPointsGained()
	{
		return pointsGained;
	}	
	
	/**
	 * @return AP required for Rank
	 */
	public int getRequired()
	{
		return required;
	}

	public static AbyssRankEnum getRankById(int id)
	{
		for(AbyssRankEnum rank : values())
		{
			if(rank.getId() == id)
				return rank;
		}
		throw new IllegalArgumentException("Invalid abyss rank provided");
	}
	
	public static AbyssRankEnum getRankForAp(int ap)
	{
		AbyssRankEnum r = AbyssRankEnum.GRADE9_SOLDIER;
		for(AbyssRankEnum rank : values())
		{
			if(rank.getRequired() <= ap)
				r = rank;
			else
				break;
		}
		return r;
	}
}
