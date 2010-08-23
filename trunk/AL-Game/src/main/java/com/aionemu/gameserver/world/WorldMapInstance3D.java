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
package com.aionemu.gameserver.world;

/**
 * @author Mr. Poke
 * 
 */
public class WorldMapInstance3D extends WorldMapInstance
{

	/**
	 * @param parent
	 * @param instanceId
	 */
	public WorldMapInstance3D(WorldMap parent, int instanceId)
	{
		super(parent, instanceId);
	}

	/**
	 * Calculate region id from cords.
	 * 
	 * @param x
	 * @param y
	 * @return region id.
	 */
	@Override
	protected Integer getRegionId(float x, float y, float z)
	{
		return ((int) x) / regionSize * maxWorldSize * maxWorldSize + ((int) y) / regionSize * maxWorldSize + ((int) z)
			/ regionSize;
	}

	/**
	 * Create new MapRegion and add link to neighbours.
	 * 
	 * @param regionId
	 * @return newly created map region
	 */
	@Override
	protected MapRegion createMapRegion(Integer regionId)
	{
		MapRegion r = new MapRegion(regionId, this, true);
		regions.put(regionId, r);

		int rx = regionId / (maxWorldSize * maxWorldSize);
		int ry = regionId / maxWorldSize % maxWorldSize;
		int rz = (regionId % maxWorldSize * maxWorldSize) / maxWorldSize;

		for(int x = rx - 1; x <= rx + 1; x++)
		{
			for(int y = ry - 1; y <= ry + 1; y++)
			{
				for(int z = rz - 1; z <= rz + 1; z++)
				{
					if(x == rx && y == ry && z == rz)
						continue;
					int neighbourId = x * maxWorldSize * maxWorldSize + y * maxWorldSize + z;

					MapRegion neighbour = regions.get(neighbourId);
					if(neighbour != null)
					{
						r.addNeighbourRegion(neighbour);
						neighbour.addNeighbourRegion(r);
					}
				}
			}
		}
		return r;
	}

}
