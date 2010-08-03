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
package com.aionemu.gameserver.model.gameobjects.player;

import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * @author ATracer
 *
 */
public class AbyssRank
{
	private int ap;
	private AbyssRankEnum rank;
	
	private PersistentState persistentState;
    private int allKill;
    private int maxRank;


    public AbyssRank(int ap, int rank, int allKill, int maxRank)
	{
		super();
		this.ap = ap;
		this.rank = AbyssRankEnum.getRankById(rank);
        this.allKill = allKill;
        this.maxRank = maxRank;
	}
	
	public void addAp(int ap)
	{	
		this.setAp(this.ap + ap);
	}
	/**
	 * @return the ap
	 */
	public int getAp()
	{
		return ap;
	}
	
	/**
	 * @param ap the ap to set
	 */
	public void setAp(int ap)
	{
		if(ap < 0)
			ap = 0;
		this.ap = ap;
		
		AbyssRankEnum newRank = AbyssRankEnum.getRankForAp(this.ap);
		if(newRank != this.rank)
			setRank(newRank);
		
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}
	
	/**
	 * @return the rank
	 */
	public AbyssRankEnum getRank()
	{
		return rank;
	}

    /**
     * @return all Kill
     */
    public int getAllKill()
    {
        return allKill;
    }

    public void setAllKill()
    {
        this.allKill = allKill+1;
    }

    /**
     * @return max Rank
     */
    public int getMaxRank()
    {
        return maxRank;
    }

	/**
	 * @param rank the rank to set
	 */
	public void setRank(AbyssRankEnum rank)
	{
        if(rank.getId() > this.maxRank)
            this.maxRank = rank.getId();
		this.rank = rank;

		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}
	
	/**
	 * @return the persistentState
	 */
	public PersistentState getPersistentState()
	{
		return persistentState;
	}
	
	/**
	 * @param persistentState the persistentState to set
	 */
	public void setPersistentState(PersistentState persistentState)
	{
		switch(persistentState)
		{
			case UPDATE_REQUIRED:
				if(this.persistentState == PersistentState.NEW)
					break;
			default:
				this.persistentState = persistentState;
		}
	}
	
}

