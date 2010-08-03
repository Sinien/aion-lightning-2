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

import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.dao.SiegeDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.siege.Fortress;
import com.aionemu.gameserver.model.siege.Influence;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SIEGE_LOCATION_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author Sarynth
 */
public class SiegeService
{
	private static Logger	log	= Logger.getLogger(SiegeService.class);
	
	public static final SiegeService getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private FastMap<Integer, SiegeLocation> locations;
	
	public SiegeService()
	{
		if (SiegeConfig.SIEGE_ENABLED)
		{
			log.info("Loading Siege Location Data...");
			
			locations = DataManager.SIEGE_LOCATION_DATA.getSiegeLocations();
			
			DAOManager.getDAO(SiegeDAO.class).loadSiegeLocations(locations);
		}
		else
		{
			log.info("Siege Disabled by Config.");
			
			locations = new FastMap<Integer, SiegeLocation>();
		}
	}
	
	public FastMap<Integer, SiegeLocation> getSiegeLocations()
	{
		return locations;
	}
	
	/**
	 * @return siege time
	 */
	public int getSiegeTime()
	{
		// TODO: (soon) Siege Timer
		//Siege Time Blocks: 7200 sec (2 hours)
		return 0;
	}
	
	/**
	 * @param locationId
	 * @return
	 */
	public SiegeLocation getSiegeLocation(int locationId)
	{
		return locations.get(locationId);
	}
	
	/**
	 * @param location id
	 * @param siege race
	 */
	public void capture(int locationId, SiegeRace race)
	{
		this.capture(locationId, race, 0);
	}
	
	/**
	 * @param location id
	 * @param siege race
	 * @param legion id
	 */
	public void capture(int locationId, SiegeRace race, int legionId)
	{
		//TODO: Convert all spawns to match new race.
		
		SiegeLocation sLoc = locations.get(locationId);
		sLoc.setRace(race);
		sLoc.setLegionId(legionId);
		
		if (sLoc instanceof Fortress)
			sLoc.setVulnerable(false);
		
		DAOManager.getDAO(SiegeDAO.class).updateSiegeLocation(sLoc);
		
		broadcastUpdate(sLoc);
		Influence.getInstance().recalculateInfluence();
	}
	
	/**
	 * @param loc
	 */
	public void broadcastUpdate(SiegeLocation loc)
	{
		SM_SIEGE_LOCATION_INFO pkt = new SM_SIEGE_LOCATION_INFO(loc);
		broadcast(pkt);
	}
	
	public void broadcastUpdate()
	{
		SM_SIEGE_LOCATION_INFO pkt = new SM_SIEGE_LOCATION_INFO();
		broadcast(pkt);
	}
	
	private void broadcast(SM_SIEGE_LOCATION_INFO pkt)
	{
		Iterator<Player> iter = World.getInstance().getPlayersIterator();
		
		while(iter.hasNext())
		{
			PacketSendUtility.sendPacket(iter.next(), pkt);
		}

	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final SiegeService instance = new SiegeService();
	}


}
