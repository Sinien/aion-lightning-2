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
package com.aionemu.gameserver.controllers;

import java.util.Iterator;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.templates.spawn.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RIFT_ANNOUNCE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RIFT_STATUS;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.services.TeleportService;
import com.aionemu.gameserver.spawnengine.RiftSpawnManager.RiftEnum;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author ATracer
 *
 */
public class RiftController extends NpcController
{
	private boolean isMaster = false;
	private SpawnTemplate slaveSpawnTemplate;
	private Npc slave;
	
	private Integer maxEntries;
	private Integer maxLevel;
	
	private int usedEntries;
	private boolean isAccepting;
	
	private RiftEnum riftTemplate;
	
	/**
	 * Used to create master rifts or slave rifts (slave == null)
	 * 
	 * @param slaveSpawnTemplate
	 */

	public RiftController(Npc slave, RiftEnum riftTemplate)
	{
		this.riftTemplate = riftTemplate;
		if(slave != null)//master rift should be created
		{
			this.slave = slave;
			this.slaveSpawnTemplate = slave.getSpawn();
			this.maxEntries = riftTemplate.getEntries();
			this.maxLevel = riftTemplate.getMaxLevel();
			
			isMaster = true;
			isAccepting = true;
		}	
	}

	@Override
	public void onDialogRequest(Player player)
	{
		if(!isMaster && !isAccepting)
			return;
		
		RequestResponseHandler responseHandler = new RequestResponseHandler(getOwner())
		{
			@Override
			public void acceptRequest(Creature requester, Player responder)
			{
				if(!isAccepting)
					return;
				
				int worldId = slaveSpawnTemplate.getWorldId();
				float x = slaveSpawnTemplate.getX();
				float y = slaveSpawnTemplate.getY();
				float z = slaveSpawnTemplate.getZ();
				
				TeleportService.teleportTo(responder, worldId, x, y, z, 0);
				usedEntries++;
				
				if(usedEntries >= maxEntries)
				{
					isAccepting = false;
					
					RespawnService.scheduleDecayTask(getOwner());
					RespawnService.scheduleDecayTask(slave);
				}
				PacketSendUtility.broadcastPacket(getOwner(), 
					new SM_RIFT_STATUS(getOwner().getObjectId(), usedEntries, maxEntries, maxLevel));
					
			}
			@Override
			public void denyRequest(Creature requester, Player responder)
			{
				//do nothing
			}
		};

		boolean requested = player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_USE_RIFT, responseHandler);
		if (requested)
		{
			PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_USE_RIFT, 0));
		}
	}

	@Override
	public void see(VisibleObject object)
	{
		if(!isMaster)
			return;
		
		if(object instanceof Player)
		{
			PacketSendUtility.sendPacket((Player) object, 
				new SM_RIFT_STATUS(getOwner().getObjectId(), usedEntries, maxEntries, maxLevel));
		}
	}

	/**
	 * @param activePlayer
	 */
	public void sendMessage(Player activePlayer)
	{
		if(isMaster && getOwner().isSpawned())
			PacketSendUtility.sendPacket(activePlayer, new SM_RIFT_ANNOUNCE(riftTemplate.getDestination()));
	}

	/**
	 * 
	 */
	public void sendAnnounce()
	{
		if(isMaster && getOwner().isSpawned())
		{
			WorldMapInstance worldInstance = getOwner().getPosition().getMapRegion().getParent();
			Iterator<Player> playerIterator = worldInstance.playerIterator();
			
			while(playerIterator.hasNext())
			{
				Player player = playerIterator.next();
				if(player.isSpawned())
				{
					sendMessage(player);
				}				
			}
		}
	}
}
