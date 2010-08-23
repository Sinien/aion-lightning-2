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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javolution.util.FastMap;

import com.aionemu.gameserver.model.templates.WorldMapTemplate;

/**
 * This object is representing one in-game map and can have instances.
 * 
 * @author -Nemesiss-
 * 
 */
public class WorldMap
{
	private WorldMapTemplate				worldMapTemplate;

	private AtomicInteger nextInstanceId = new AtomicInteger(0);
	/**
	 * List of instances.
	 */
	private Map<Integer, WorldMapInstance>	instances	= new FastMap<Integer, WorldMapInstance>().shared();

	/** World to which belongs this WorldMap */
	private World world;
	
	public WorldMap(WorldMapTemplate worldMapTemplate, World world)
	{
		this.world = world;
		this.worldMapTemplate = worldMapTemplate;
		
		if(worldMapTemplate.getTwinCount() != 0)
		{
			for(int i = 1; i <= worldMapTemplate.getTwinCount(); i++)
			{
				int nextId = getNextInstanceId();
				addInstance(nextId);	
			}			
		}	
		else
		{
			int nextId = getNextInstanceId();
			addInstance(nextId);
		}
	}

	/**
	 * Returns map name
	 * 
	 * @return map name
	 */
	public String getName()
	{
		return worldMapTemplate.getName();
	}
	
	/**
	 * Returns water level on map
	 * @return water level
	 */
	public int getWaterLevel()
	{
		return worldMapTemplate.getWaterLevel();
	}
	
	/**
	 * Returns death level on map
	 * @return death level
	 */
	public int getDeathLevel()
	{
		return worldMapTemplate.getDeathLevel();
	}
	
	/**
	 * Returns the WorldType of the map
	 * @return world type
	 */
	public WorldType getWorldType()
	{
		return worldMapTemplate.getWorldType();
	}

	/**
	 * Returns map id
	 * 
	 * @return map id
	 */
	public Integer getMapId()
	{
		return worldMapTemplate.getMapId();
	}
	
	/**
	 * 
	 * @return int
	 */
	public int getInstanceCount()
	{
		int twinCount = worldMapTemplate.getTwinCount();
		return twinCount > 0 ? twinCount : 1;
	}

	/**
	 * Return a WorldMapInstance - depends on map configuration one map may have twins instances to balance player. This
	 * method will return WorldMapInstance by server chose.
	 *  
	 *  
	 * @return WorldMapInstance.
	 */
	public WorldMapInstance getWorldMapInstance()
	{
		//TODO Balance players into instances.
		return getWorldMapInstance(1);
	}
	
	/**
	 *  This method return WorldMapInstance by specified instanceId
	 *  
	 * @param instanceId
	 * @return WorldMapInstance
	 */
	public WorldMapInstance getWorldMapInstanceById(int instanceId)
	{
		if(worldMapTemplate.getTwinCount() !=0)
		{
			if(instanceId > worldMapTemplate.getTwinCount())
			{
				throw new IllegalArgumentException("WorldMapInstance " + worldMapTemplate.getMapId() + " has lower instances count than " + instanceId);
			}		
		}
		return getWorldMapInstance(instanceId);
	}

	/**
	 * Returns WorldMapInstance by instanceId.
	 * 
	 * @param instanceId
	 * @return WorldMapInstance/
	 */
	private WorldMapInstance getWorldMapInstance(int instanceId)
	{
		return instances.get(instanceId);
	}

	/**
	 * Remove WorldMapInstance by instanceId.
	 * 
	 * @param instanceId
	 */
	public void removeWorldMapInstance(int instanceId)
	{
		instances.remove(instanceId);
	}
	
	/**
	 *  Add instance to map and increases pointer to nextInstanceId
	 *  
	 * @param instanceId
	 * @param instance
	 */
	public WorldMapInstance addInstance(int instanceId)
	{
		WorldMapInstance worldMapInstance = null;
		if (this.worldMapTemplate.getMapId() == WorldMapType.RESHANTA.getId())
			worldMapInstance = new WorldMapInstance3D(this, instanceId);
		else
			worldMapInstance = new WorldMapInstance(this, instanceId);
		
		instances.put(instanceId, worldMapInstance);
		return worldMapInstance;
	}
	/**
	 * Returns the World containing this WorldMap.
	 */
	public World getWorld()
	{
		return world;
	}

	/**
	 * @return the nextInstanceId
	 */
	public int getNextInstanceId()
	{
		return nextInstanceId.incrementAndGet();
	}

	/**
	 *  Whether this world map is instance type
	 *  
	 * @return
	 */
	public boolean isInstanceType()
	{
		return worldMapTemplate.isInstance();
	}
	
	/**
	 * @return
	 */
	public Collection<WorldMapInstance> getAllWorldMapInstances()
	{
		return instances.values();
	}
}
