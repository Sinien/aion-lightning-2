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

import java.util.Map;

import javolution.util.FastMap;

import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.utils.MathUtil;

/**
 * KnownList.
 * 
 * @author -Nemesiss-, kosyachok, lord_rex
 * 		based on l2j-free engines.
 */
public class KnownList
{
	// how far player will see visible object
	private static final float					visibilityDistance	= 95;

	// maxZvisibleDistance
	private static final float					maxZvisibleDistance	= 95;

	private final VisibleObject					owner;

	private final Map<Integer, VisibleObject>	knownObjects		= new FastMap<Integer, VisibleObject>().shared();

	private long								lastUpdate;

	/**
	 * Constructor.
	 * 
	 * @param owner
	 */
	public KnownList(VisibleObject owner)
	{
		this.owner = owner;
	}

	/**
	 * Owner of this KnownList.
	 */
	public VisibleObject getOwner()
	{
		return owner;
	}

	/**
	 * List of objects that this KnownList owner known
	 */
	public Map<Integer, VisibleObject> getKnownObjects()
	{
		return knownObjects;
	}

	/**
	 * Do KnownList update.
	 */
	public synchronized final void updateKnownList()
	{
		if((System.currentTimeMillis() - lastUpdate) < 100)
			return;
		
		updateKnownListImpl();
		
		lastUpdate = System.currentTimeMillis();
	}
	
	protected void updateKnownListImpl()
	{
		forgetObjects();
		findVisibleObjects();
	}

	/**
	 * Clear known list. Used when object is despawned.
	 */
	public final void clearKnownList()
	{
		for(VisibleObject object : getKnownObjects().values())
		{
			object.getKnownList().removeKnownObject(getOwner(), false);
		}

		getKnownObjects().clear();
	}

	/**
	 * Check if object is known
	 * 
	 * @param object
	 * @return true if object is known
	 */
	public boolean knowns(AionObject object)
	{
		return getKnownObjects().containsKey(object.getObjectId());
	}

	/**
	 * Add VisibleObject to this KnownList. Object is unknown.
	 * 
	 * @param object
	 */
	protected void addKnownObject(VisibleObject object)
	{
		if(getKnownObjects().put(object.getObjectId(), object) == null)
			getOwner().getController().see(object);
	}

	/**
	 * Remove VisibleObject from this KnownList. Object was known.
	 * 
	 * @param object
	 */
	private final void removeKnownObject(VisibleObject object, boolean isOutOfRange)
	{
		if(getKnownObjects().remove(object.getObjectId()) != null)
			getOwner().getController().notSee(object, isOutOfRange);
	}

	/**
	 * forget out of distance objects.
	 */
	private final void forgetObjects()
	{
		for(VisibleObject object : getKnownObjects().values())
		{
			if(!checkObjectInRange(getOwner(), object))
			{
				getOwner().getController().notSee(object, true);
				object.getKnownList().removeKnownObject(getOwner(), true);
			}
		}
	}

	/**
	 * Find objects that are in visibility range.
	 */
	protected void findVisibleObjects()
	{
		if(getOwner() == null || !getOwner().isSpawned())
			return;

		for(MapRegion region : getOwner().getActiveRegion().getNeighbours())
		{
			for(VisibleObject newObject : region.getVisibleObjects().values())
			{
				if(newObject == getOwner() || newObject == null)
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

	protected final boolean checkObjectInRange(VisibleObject owner, VisibleObject newObject)
	{
		// check if Z distance is greater than maxZvisibleDistance
		if(Math.abs(owner.getZ() - newObject.getZ()) > maxZvisibleDistance)
			return false;

		return MathUtil.isInRange(owner, newObject, visibilityDistance);
	}
}
