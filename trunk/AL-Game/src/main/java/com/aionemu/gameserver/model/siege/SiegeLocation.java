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
package com.aionemu.gameserver.model.siege;

import com.aionemu.gameserver.model.templates.siegelocation.SiegeLocationTemplate;


/**
 * @author Sarynth
 *
 */
public class SiegeLocation
{
	public static final int	INVULNERABLE	= 0;
	public static final int	VULNERABLE	= 1;
	
	/**
	 * Unique id, defined by NCSoft
	 */
	private int locationId;
	private int worldId;
	
	private SiegeRace siegeRace = SiegeRace.BALAUR;
	private int legionId = 0;
	
	private boolean isVulnerable = false;
	private int nextState = 0;
	
	public SiegeLocation() {} // <3 Fastmap
	public SiegeLocation(SiegeLocationTemplate template)
	{
		this.locationId = template.getId();
		this.worldId = template.getWorldId();
		
	}
	
	/**
	 * Returns unique LocationId of Siege Location
	 * @return Integer LocationId
	 */
	public int getLocationId()
	{
		return this.locationId;
	}
	
	public int getWorldId()
	{
		return this.worldId;
	}
	
	public SiegeRace getRace()
	{
		return this.siegeRace;
	}
	
	public void setRace(SiegeRace siegeRace)
	{
		this.siegeRace = siegeRace;
	}
	
	public int getLegionId()
	{
		return this.legionId;
	}
	
	public void setLegionId(int legionId)
	{
		this.legionId = legionId;
	}

	/**
	 * Next State:
	 * 		0 invulnerable
	 * 		1 vulnerable
	 * @return nextState
	 */
	public int getNextState()
	{
		return this.nextState;
	}
	
	/**
	 * @param nextState
	 */
	public void setNextState(Integer nextState)
	{
		this.nextState = nextState;
	}
	
	/**
	 * @return isVulnerable
	 */
	public boolean isVulnerable()
	{
		return this.isVulnerable;
	}
	
	/**
	 * @param new vulnerable value
	 */
	public void setVulnerable(boolean value)
	{
		this.isVulnerable = value;
	}
	/**
	 * @return
	 */
	public int getInfluenceValue()
	{
		return 0;
	}
}
