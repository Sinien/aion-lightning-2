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
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javolution.util.FastMap;

import com.aionemu.gameserver.configs.main.OptionsConfig;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.group.PlayerGroup;
import com.aionemu.gameserver.world.exceptions.DuplicateAionObjectException;

/**
 * World map instance object.
 * 
 * @author -Nemesiss-
 * 
 */
public class WorldMapInstance
{
	/**
	 * Size of region
	 */
	public static final int						regionSize			= OptionsConfig.REGION_SIZE;
	/**
	 * Max world size - actually it must be some value bigger than world size. Used only for id generation.
	 */
	protected static final int					maxWorldSize		= OptionsConfig.MAX_WORLD_SIZE;
	/**
	 * WorldMap witch is parent of this instance.
	 */
	private final WorldMap						parent;
	/**
	 * Map of active regions.
	 */
	protected final Map<Integer, MapRegion>		regions				= new FastMap<Integer, MapRegion>().shared();

	/**
	 * All objects spawned in this world map instance
	 */
	private final Map<Integer, VisibleObject>	worldMapObjects		= new FastMap<Integer, VisibleObject>().shared();

	/**
	 * All players spawned in this world map instance
	 */
	private final Map<Integer, Player>			worldMapPlayers		= new FastMap<Integer, Player>().shared();

	private final Set<Integer>					registeredObjects	= Collections
																		.newSetFromMap(new FastMap<Integer, Boolean>()
																			.shared());

	private PlayerGroup							registeredGroup		= null;

	private Future<?>							emptyInstanceTask	= null;

	/**
	 * Id of this instance (channel)
	 */
	private int									instanceId;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 */
	public WorldMapInstance(WorldMap parent, int instanceId)
	{
		this.parent = parent;
		this.instanceId = instanceId;
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

	/**
	 * Returns WorldMap witch is parent of this instance
	 * 
	 * @return parent
	 */
	public WorldMap getParent()
	{
		return parent;
	}

	/**
	 * Returns MapRegion that contains coordinates of VisibleObject. If the region doesn't exist, it's created.
	 *
	 * @param object
	 *
	 * @return a MapRegion
	 */
	MapRegion getRegion(VisibleObject object)
	{
		return getRegion(object.getX(), object.getY(), object.getZ());
	}

	/**
	 * Returns MapRegion that contains given x,y coordinates. If the region doesn't exist, it's created.
	 *
	 * @param x
	 * @param y
	 * @return a MapRegion
	 */
	MapRegion getRegion(float x, float y, float z)
	{
		Integer regionId = getRegionId(x, y, z);
		MapRegion region = regions.get(regionId);
		if(region == null)
		{
			synchronized(this)
			{
				region = regions.get(regionId);
				if(region == null)
				{
					region = createMapRegion(regionId);
				}
			}
		}
		return region;
	}

	/**
	 * Calculate region id from cords.
	 *
	 * @param x
	 * @param y
	 * @return region id.
	 */
	protected Integer getRegionId(float x, float y, float z)
	{
		return ((int) x) / regionSize * maxWorldSize + ((int) y) / regionSize;
	}

	/**
	 * Create new MapRegion and add link to neighbours.
	 *
	 * @param regionId
	 * @return newly created map region
	 */
	protected MapRegion createMapRegion(Integer regionId)
	{
		MapRegion r = new MapRegion(regionId, this, false);
		regions.put(regionId, r);

		int rx = regionId / maxWorldSize;
		int ry = regionId % maxWorldSize;

		for(int x = rx - 1; x <= rx + 1; x++)
		{
			for(int y = ry - 1; y <= ry + 1; y++)
			{
				if(x == rx && y == ry)
					continue;
				int neighbourId = x * maxWorldSize + y;

				MapRegion neighbour = regions.get(neighbourId);
				if(neighbour != null)
				{
					r.addNeighbourRegion(neighbour);
					neighbour.addNeighbourRegion(r);
				}
			}
		}
		return r;
	}

	/**
	 * Returs {@link World} instance to which belongs this WorldMapInstance
	 * 
	 * @return World
	 */
	public World getWorld()
	{
		return getParent().getWorld();
	}

	/**
	 * 
	 * @param object
	 */
	public void addObject(VisibleObject object)
	{
		if(worldMapObjects.put(object.getObjectId(), object) != null)
			throw new DuplicateAionObjectException();

		if(object instanceof Player)
			worldMapPlayers.put(object.getObjectId(), (Player) object);
	}

	/**
	 * 
	 * @param object
	 */
	public void removeObject(AionObject object)
	{
		worldMapObjects.remove(object.getObjectId());
		if(object instanceof Player)
			worldMapPlayers.remove(object.getObjectId());
	}

	/**
	 * @return the instanceIndex
	 */
	public int getInstanceId()
	{
		return instanceId;
	}

	/**
	 * Check player is in instance
	 * 
	 * @param objId
	 * @return
	 */
	public boolean isInInstance(int objId)
	{
		return worldMapPlayers.containsKey(objId);
	}

	public Collection<VisibleObject> getAllWorldMapObjects()
	{
		return worldMapObjects.values();
	}

	public Collection<Player> getAllWorldMapPlayers()
	{
		return worldMapPlayers.values();
	}

	public void registerGroup(PlayerGroup group)
	{
		registeredGroup = group;
		register(group.getGroupId());
	}

	/**
	 * @param objectId
	 */
	public void register(int objectId)
	{
		registeredObjects.add(objectId);
	}

	/**
	 * @return the registeredObjects
	 */
	public int getRegisteredObjectsSize()
	{
		return registeredObjects.size();
	}

	/**
	 * @param objectId
	 * @return
	 */
	public boolean isRegistered(int objectId)
	{
		return registeredObjects.contains(objectId);
	}

	/**
	 * @return the emptyInstanceTask
	 */
	public Future<?> getEmptyInstanceTask()
	{
		return emptyInstanceTask;
	}

	/**
	 * @param emptyInstanceTask
	 *            the emptyInstanceTask to set
	 */
	public void setEmptyInstanceTask(Future<?> emptyInstanceTask)
	{
		this.emptyInstanceTask = emptyInstanceTask;
	}

	/**
	 * @return the registeredGroup
	 */
	public PlayerGroup getRegisteredGroup()
	{
		return registeredGroup;
	}

	/**
	 * 
	 * @return
	 */
	public int playersCount()
	{
		return worldMapPlayers.size();
	}
}
