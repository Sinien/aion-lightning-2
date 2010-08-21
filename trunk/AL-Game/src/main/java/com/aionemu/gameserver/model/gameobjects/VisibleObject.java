/*
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
package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.VisibleObjectController;
import com.aionemu.gameserver.model.templates.VisibleObjectTemplate;
import com.aionemu.gameserver.model.templates.spawn.SpawnTemplate;
import com.aionemu.gameserver.world.KnownList;
import com.aionemu.gameserver.world.MapRegion;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.WorldType;

/**
 * This class is representing visible objects. It's a base class for all in-game objects that can be spawned in the
 * world at some particular position (such as players, npcs).<br>
 * <br>
 * Objects of this class, as can be spawned in game, can be seen by other visible objects. To keep track of which
 * objects are already "known" by this visible object and which are not, VisibleObject is containing {@link KnownList}
 * which is responsible for holding this information.
 * 
 * @author -Nemesiss-
 * 
 */
public abstract class VisibleObject extends AionObject
{
	protected VisibleObjectTemplate objectTemplate;
	
	/**
	 * Constructor.
	 * 
	 * @param objId
	 * @param objectTemplate 
	 */
	public VisibleObject(int objId, VisibleObjectController<? extends VisibleObject> controller, SpawnTemplate spawnTemplate, VisibleObjectTemplate objectTemplate, WorldPosition position)
	{
		super(objId);
		this.controller = controller;
		this.position = position;
		this.spawn = spawnTemplate;
		this.objectTemplate = objectTemplate;
	}

	/**
	 * Position of object in the world.
	 */
	private WorldPosition											position;

	/**
	 * KnownList of this VisibleObject.
	 */
	private KnownList												knownlist;

	/**
	 * Controller of this VisibleObject
	 */
	private final VisibleObjectController<? extends VisibleObject>	controller;
	
	/**
	 * Visible object's target
	 */
	private VisibleObject	target;
	
	/**
	 *  Spawn template of this visibleObject. .
	 */
	private SpawnTemplate	spawn;

	/**
	 * Returns current WorldRegion AionObject is in.
	 * 
	 * @return mapRegion
	 */
	public MapRegion getActiveRegion()
	{
		return position.getMapRegion();
	}
	
	public int getInstanceId()
	{
		return position.getInstanceId();
	}

	/**
	 * Return World map id.
	 * 
	 * @return world map id
	 */
	public int getWorldId()
	{
		return position.getMapId();
	}

	/**
	 * Return WorldType of current location
	 * 
	 * @return WorldType of current location
	 */
	public WorldType getWorldType()
	{
		return World.getInstance().getWorldMap(getWorldId()).getWorldType();
	}
	
	/**
	 * Return World position x
	 * 
	 * @return x
	 */
	public float getX()
	{
		return position.getX();
	}

	/**
	 * Return World position y
	 * 
	 * @return y
	 */
	public float getY()
	{
		return position.getY();
	}

	/**
	 * Return World position z
	 * 
	 * @return z
	 */
	public float getZ()
	{
		return position.getZ();
	}

	/**
	 * Heading of the object. Values from <0,120)
	 * 
	 * @return heading
	 */
	public byte getHeading()
	{
		return position.getHeading();
	}

	/**
	 * Return object position
	 * 
	 * @return position.
	 */
	public WorldPosition getPosition()
	{
		return position;
	}

	/**
	 * Check if object is spawned.
	 * 
	 * @return true if object is spawned.
	 */
	public boolean isSpawned()
	{
		return position.isSpawned();
	}
	
	/**
	 * 
	 * @return 
	 */
	public boolean isInWorld()
	{
		return World.getInstance().findAionObject(getObjectId()) != null;
	}
	
	/**
	 * Check if map is instance
	 * 
	 * @return true if object in one of the instance maps
	 */
	public boolean isInInstance()
	{
		return position.isInstanceMap();
	}

	/**
	 * Set KnownList to this VisibleObject
	 * 
	 * @param knownlist
	 */
	public void setKnownlist(KnownList knownlist)
	{
		this.knownlist = knownlist;
	}

	/**
	 * Returns KnownList of this VisibleObject.
	 * 
	 * @return knownList.
	 */
	public KnownList getKnownList()
	{
		return knownlist;
	}

	/**
	 * Return VisibleObjectController of this VisibleObject
	 * 
	 * @return VisibleObjectController.
	 */
	public VisibleObjectController<? extends VisibleObject> getController()
	{
		return controller;
	}
	
	/**
	 * 
	 * @return VisibleObject
	 */
	public VisibleObject getTarget()
	{
		return target;
	}
	
	/**
	 * 
	 * @param creature
	 */
	public void setTarget(VisibleObject creature)
	{
		target = creature;
	}
	
	/**
	 * 
	 * @param objectId
	 * @return target is object with id equal to objectId
	 */
	public boolean isTargeting(int objectId)
	{
		return target != null && target.getObjectId() == objectId; 
	}
	
	/**
	 *  Return spawn template of this VisibleObject
	 *  
	 * @return SpawnTemplate
	 */
	public SpawnTemplate getSpawn()
	{
		return spawn;
	}

	/**
	 * @param spawn the spawn to set
	 */
	public void setSpawn(SpawnTemplate spawn)
	{
		this.spawn = spawn;
	}

	/**
	 * @return the objectTemplate
	 */
	public VisibleObjectTemplate getObjectTemplate()
	{
		return objectTemplate;
	}

	/**
	 * @param objectTemplate the objectTemplate to set
	 */
	public void setObjectTemplate(VisibleObjectTemplate objectTemplate)
	{
		this.objectTemplate = objectTemplate;
	}
}