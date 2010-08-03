/**
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
package com.aionemu.gameserver.world;

import javolution.util.FastList;
import javolution.util.FastMap;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * Just some part of map.
 * 
 * @author -Nemesiss-
 * 
 */
public class MapRegion
{
	/**
	 * Region id of this map region [NOT WORLD ID!]
	 */
	private final Integer				regionId;
	/**
	 * WorldMapInstance witch is parent of this map region.
	 */
	private final WorldMapInstance		parent;
	/**
	 * Surrounding regions + self.
	 */
	private final FastList<MapRegion>		neighbours	= new FastList<MapRegion>(9);
	/**
	 * Objects on this map region.
	 */
	private final FastMap<Integer, VisibleObject> 	objects	= new FastMap<Integer, VisibleObject>().shared();

	private int playerCount = 0;
	
	private boolean regionActive = false;
	/**
	 * Constructor.
	 * 
	 * @param id
	 * @param parent
	 */
	MapRegion(Integer id, WorldMapInstance parent)
	{
		this.regionId = id;
		this.parent = parent;
		this.neighbours.add(this);
	}

	/**
	 * Return World map id.
	 * 
	 * @return world map id
	 */
	public Integer getMapId()
	{
		return getParent().getMapId();
	}

	/** Return an instance of {@link World}, which keeps map, to which belongs this region */
	public World getWorld()
	{
		return getParent().getWorld();
	}
	/**
	 * Returns region id of this map region. [NOT WORLD ID!]
	 * 
	 * @return region id.
	 */
	public Integer getRegionId()
	{
		return regionId;
	}

	/**
	 * Returns WorldMapInstance witch is parent of this instance
	 * 
	 * @return parent
	 */
	public WorldMapInstance getParent()
	{
		return parent;
	}

	/**
	 * Returns iterator over AionObjects on this region
	 * 
	 * @return objects iterator
	 */
	public FastMap<Integer, VisibleObject> getObjects()
	{
		return objects;
	}

	/**
	 * @return the neighbours
	 */
	public FastList<MapRegion> getNeighbours()
	{
		return neighbours;
	}

	/**
	 * Add neighbour region to this region neighbours list.
	 * 
	 * @param neighbour
	 */
	void addNeighbourRegion(MapRegion neighbour)
	{
		neighbours.add(neighbour);
	}

	/**
	 * Add AionObject to this region objects list.
	 * 
	 * @param object
	 */
	void add(VisibleObject object)
	{
		if (objects.put(object.getObjectId(), object) == null)
		{
			if (object instanceof Player)
			{
				playerCount++;
				regionActive = playerCount > 0;
			}
		}
	}

	/**
	 * Remove AionObject from region objects list.
	 * 
	 * @param object
	 */
	void remove(VisibleObject object)
	{
		if (objects.remove(object.getObjectId()) != null)
			if (object instanceof Player)
			{
				playerCount--;
				regionActive = playerCount > 0;
			}
	}
	
	public boolean isMapRegionActive()
	{
		for (FastList.Node<MapRegion> n = neighbours.head(), end = neighbours.tail(); (n = n.getNext()) != end;)
		{
			MapRegion r = n.getValue();
			if (r.regionActive)
				return true;
		}
		return false;
	}
}
