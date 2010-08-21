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

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Mr. Poke
 * 
 */
public class StaticObjectKnownList extends KnownList
{
	/**
	 * @param owner
	 */
	public StaticObjectKnownList(VisibleObject owner)
	{
		super(owner);
	}

	/**
	 * Add VisibleObject to this KnownList.
	 * 
	 * @param object
	 */
	@Override
	protected final void addKnownObject(VisibleObject object)
	{
		if(object instanceof Player)
			super.addKnownObject(object);
	}

	/**
	 * Find objects that are in visibility range.
	 */
	@Override
	protected final void findVisibleObjects()
	{
		if(getOwner() == null || !getOwner().isSpawned())
			return;

		for(MapRegion region : getOwner().getActiveRegion().getNeighbours())
		{
			for(VisibleObject newObject : region.getVisibleObjects().values())
			{
				if(newObject == getOwner() || newObject == null)
					continue;

				if(!(newObject instanceof Player))
					continue;

				if(!checkObjectInRange(getOwner(), newObject))
					continue;

				if(getKnownObjects().put(newObject.getObjectId(), newObject) == null)
				{
					newObject.getKnownList().addKnownObject(getOwner());
					getOwner().getController().see(newObject);
				}
			}
		}
	}
}
