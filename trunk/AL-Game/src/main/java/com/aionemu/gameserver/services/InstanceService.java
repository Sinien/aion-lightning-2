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
package com.aionemu.gameserver.services;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.group.PlayerGroup;
import com.aionemu.gameserver.model.templates.WorldMapTemplate;
import com.aionemu.gameserver.model.templates.portal.EntryPoint;
import com.aionemu.gameserver.model.templates.portal.PortalTemplate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMap;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author ATracer
 * 
 */
public class InstanceService
{
	private static Logger	log	= Logger.getLogger(InstanceService.class);

	/**
	 * @param worldId
	 * @param destroyTime
	 * @return
	 */
	public synchronized static WorldMapInstance getNextAvailableInstance(int worldId)
	{
		WorldMap map = World.getInstance().getWorldMap(worldId);

		if(!map.isInstanceType())
			throw new UnsupportedOperationException("Invalid call for next available instance  of " + worldId);

		int nextInstanceId = map.getNextInstanceId();

		log.info("Creating new instance: " + worldId + " " + nextInstanceId);

		WorldMapInstance worldMapInstance = new WorldMapInstance(map, nextInstanceId);
		startInstanceChecker(worldMapInstance);
		map.addInstance(nextInstanceId, worldMapInstance);
		SpawnEngine.getInstance().spawnInstance(worldId, worldMapInstance.getInstanceId());
		
		return worldMapInstance;
	}

	/**
	 * Instance will be destroyed All players moved to bind location All objects - deleted
	 */
	private static void destroyInstance(WorldMapInstance instance)
	{
		instance.getEmptyInstanceTask().cancel(false);
		
		int worldId = instance.getMapId();
		int instanceId = instance.getInstanceId();

		WorldMap map = World.getInstance().getWorldMap(worldId);
		map.removeWorldMapInstance(instanceId);

		log.info("Destroying instance:" + worldId + " " + instanceId);

		Iterator<VisibleObject> it = instance.objectIterator();
		while(it.hasNext())
		{
			VisibleObject obj = it.next();
			if(obj instanceof Player)
			{			
				Player player = (Player) obj;
				PortalTemplate portal = DataManager.PORTAL_DATA.getInstancePortalTemplate(worldId, player.getCommonData().getRace());
				moveToEntryPoint((Player) obj, portal, true);
			}
			else
			{
				obj.getController().delete();
			}
		}
	}
	
	/**
	 * 
	 * @param instance
	 * @param player
	 */
	public static void registerPlayerWithInstance(WorldMapInstance instance, Player player)
	{
		instance.register(player.getObjectId());
	}
	
	/**
	 * 
	 * @param instance
	 * @param group
	 */
	public static void registerGroupWithInstance(WorldMapInstance instance, PlayerGroup group)
	{
		instance.registerGroup(group);
	}
	
	/**
	 * 
	 * @param worldId
	 * @param objectId
	 * @return instance or null
	 */
	public static WorldMapInstance getRegisteredInstance(int worldId, int objectId)
	{
		Iterator<WorldMapInstance> iterator = World.getInstance().getWorldMap(worldId).iterator();
		while(iterator.hasNext())
		{
			WorldMapInstance instance = iterator.next();
			if(instance.isRegistered(objectId))
				return instance;
		}
		return null;
	}

	/**
	 * @param player
	 */
	public static void onPlayerLogin(Player player)
	{
		int worldId = player.getWorldId();
		
		WorldMapTemplate worldTemplate = DataManager.WORLD_MAPS_DATA.getTemplate(worldId);
		if(worldTemplate.isInstance())
		{
			PortalTemplate portalTemplate = DataManager.PORTAL_DATA.getInstancePortalTemplate(worldId, player.getCommonData().getRace());
			
			if (portalTemplate == null)
			{
				log.error("No portal template found for " + worldId);
				return;
			}
			
			int lookupId = player.getObjectId();
			if(portalTemplate.isGroup() && player.getPlayerGroup() != null)
			{
				lookupId = player.getPlayerGroup().getGroupId();
			}
			
			WorldMapInstance registeredInstance = getRegisteredInstance(worldId, lookupId);
			if(registeredInstance != null)
			{
				World.getInstance().setPosition(player, worldId, registeredInstance.getInstanceId(), player.getX(), player.getY(),
					player.getZ(), player.getHeading());
				return;
			}
			
			moveToEntryPoint(player, portalTemplate, false);			
		}
	}
	
	/**
	 * 
	 * @param player
	 * @param portalTemplates
	 */
	private static void moveToEntryPoint(Player player, PortalTemplate portalTemplate, boolean useTeleport)
	{		
		EntryPoint entryPoint = null;
		List<EntryPoint> entryPoints = portalTemplate.getEntryPoint();

		for(EntryPoint point : entryPoints)
		{
			if(point.getRace() == null || point.getRace().equals(player.getCommonData().getRace()))
			{
				entryPoint = point;
				break;
			}
		}
		
		if(entryPoint == null)
		{
			log.warn("Entry point not found for " + player.getCommonData().getRace() + " " + player.getWorldId());
			return;
		}
		
		if(useTeleport)
		{
			TeleportService.teleportTo(player, entryPoint.getMapId(), 1,  entryPoint.getX(), entryPoint.getY(),
				entryPoint.getZ(), 0);
		}
		else
		{
			World.getInstance().setPosition(player, entryPoint.getMapId(), 1, entryPoint.getX(), entryPoint.getY(),
				entryPoint.getZ(), player.getHeading());
		}	
		
	}

	/**
	 * @param worldId
	 * @param instanceId
	 * @return
	 */
	public static boolean isInstanceExist(int worldId, int instanceId)
	{
		return World.getInstance().getWorldMap(worldId).getWorldMapInstanceById(instanceId) != null;
	}
	
	/**
	 * 
	 * @param worldMapInstance
	 */
	private static void startInstanceChecker(WorldMapInstance worldMapInstance)
	{
		int delay = 60000 + Rnd.get(-10, 10);
		worldMapInstance.setEmptyInstanceTask(ThreadPoolManager.getInstance().scheduleAtFixedRate(
			new EmptyInstanceCheckerTask(worldMapInstance), delay, delay));
	}

	private static class EmptyInstanceCheckerTask implements Runnable
	{
		private WorldMapInstance worldMapInstance;

		private EmptyInstanceCheckerTask(WorldMapInstance worldMapInstance)
		{
			this.worldMapInstance = worldMapInstance;
		}

		@Override
		public void run()
		{
			PlayerGroup registeredGroup = worldMapInstance.getRegisteredGroup();
			if(registeredGroup == null)
			{
				if(worldMapInstance.playersCount() == 0)
				{
					destroyInstance(worldMapInstance);
					return;
				}
				Iterator<Player> playerIterator = worldMapInstance.playerIterator();
				int mapId = worldMapInstance.getMapId();
				while(playerIterator.hasNext())
				{
					Player player = playerIterator.next();
					if(player.isOnline() && player.getWorldId() == mapId)
					{
						return;
					}
				}
				destroyInstance(worldMapInstance);
			}
			else if(registeredGroup.size() == 0)
			{
				destroyInstance(worldMapInstance);
			}
		}
	}
}
