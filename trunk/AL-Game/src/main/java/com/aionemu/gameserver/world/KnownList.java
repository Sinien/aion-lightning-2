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

import java.util.Iterator;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.utils.MathUtil;

/**
 * KnownList.
 * 
 * @author -Nemesiss-
 * @modified kosyachok
 */

public class KnownList implements Iterable<VisibleObject>
{
	/**
	 * Visibility distance.
	 */

	// how far player will see visible object
	private static final float						VisibilityDistance			= 95;
	
	// maxZvisibleDistance
	private static final float						maxZvisibleDistance 		= 95;

	/**
	 * Owner of this KnownList.
	 */
	protected final VisibleObject						owner;
	/**
	 * List of objects that this KnownList owner known
	 */
	protected final Map<Integer, VisibleObject>	knownObjects	= new FastMap<Integer, VisibleObject>().shared();
	
	private long									lastUpdate;

	/**
	 * COnstructor.
	 * 
	 * @param owner
	 */
	public KnownList(VisibleObject owner)
	{
		this.owner = owner;
	}

	/**
	 * Do KnownList update.
	 */
	public void doUpdate()
	{
		if((System.currentTimeMillis() - lastUpdate) < 1000)
			return;

		forgetObjects();
		findVisibleObjects();

		lastUpdate = System.currentTimeMillis();
	}

	/**
	 * Clear known list. Used when object is despawned.
	 */
	public void clear()
	{
		Iterator<VisibleObject> knownIt = iterator();
		while(knownIt.hasNext())
		{
			VisibleObject obj = knownIt.next();
			knownIt.remove();
			obj.getKnownList().del(owner, false);
		}
	}

	/**
	 * Check if object is known
	 * 
	 * @param object
	 * @return true if object is known
	 */
	public boolean knowns(AionObject object)
	{
		return knownObjects.containsKey(object.getObjectId());
	}

	/**
	 * Returns an iterator over VisibleObjects on this known list
	 * 
	 * @return objects iterator
	 */
	@Override
	public Iterator<VisibleObject> iterator()
	{
		return knownObjects.values().iterator();
	}

	/**
	 * Add VisibleObject to this KnownList.
	 * 
	 * @param object
	 */
	protected void add(VisibleObject object)
	{
		/**
		 * object is not known.
		 */
		if(knownObjects.put(object.getObjectId(), object) == null)
			owner.getController().see(object);
	}

	/**
	 * Delete VisibleObject from this KnownList.
	 * 
	 * @param object
	 */
	private void del(VisibleObject object, boolean isOutOfRange)
	{
		/**
		 * object was known.
		 */
		if(knownObjects.remove(object.getObjectId()) != null)
			owner.getController().notSee(object, isOutOfRange);
	}

	/**
	 * forget out of distance objects.
	 */
	private void forgetObjects()
	{
		Iterator<VisibleObject> knownIt = iterator();

		while(knownIt.hasNext())
		{
			VisibleObject obj = knownIt.next();

			if(!checkObjectInRange(owner, obj))
			{
				knownIt.remove();
				owner.getController().notSee(obj, true);
				obj.getKnownList().del(owner, true);
			}
		}
	}

	/**
	 * Find objects that are in visibility range.
	 */
	protected void findVisibleObjects()
	{
		if(owner == null || !owner.isSpawned())
			return;

		FastList<MapRegion> list = owner.getActiveRegion().getNeighbours();
		for (FastList.Node<MapRegion> n = list.head(), end = list.tail(); (n = n.getNext()) != end;)
		{
			MapRegion r = n.getValue();
			FastMap<Integer, VisibleObject> objects = r.getObjects();
			for (FastMap.Entry<Integer, VisibleObject> e = objects.head(), mapEnd = objects.tail(); (e = e.getNext()) != mapEnd;)
			{
				VisibleObject newObject = e.getValue();
				if(newObject == owner || newObject == null)
					continue;
				
				if(!checkObjectInRange(owner, newObject))
					continue;

				/**
				 * New object is not known.
				 */
				if(knownObjects.put(newObject.getObjectId(), newObject) == null)
				{
					newObject.getKnownList().add(owner);
					owner.getController().see(newObject);
				}
			}
		}
	}

	protected boolean checkObjectInRange(VisibleObject owner, VisibleObject newObject)
	{
		//check if Z distance is greater than maxZvisibleDistance		
		if(Math.abs(owner.getZ() - newObject.getZ()) > maxZvisibleDistance)
			return false;				

			return MathUtil.isInRange(owner, newObject, VisibilityDistance);
	}
}
