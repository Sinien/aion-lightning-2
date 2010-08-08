/**
 * This file is part of aion-unique <aion-unique.org>.
 *
 * aion-unique is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aion-unique is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javolution.util.FastSet;

import org.apache.log4j.Logger;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.AnnouncementsDAO;
import com.aionemu.gameserver.model.Announcement;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * Automatic Announcement System
 *
 * @author Divinity
 */
public class AnnouncementService
{
	/**
	 * Logger for this class.
	 */
	private static final Logger	log		= Logger.getLogger(AnnouncementService.class);

	private Collection<Announcement>	announcements;
	private List<Future<?>>		delays	= new ArrayList<Future<?>>();
	
	private AnnouncementService()
	{
		this.load();
	}

	public static final AnnouncementService getInstance()
	{
		return SingletonHolder.instance;
	}

	/**
	 * Reload the announcements system
	 */
	public void reload()
	{
		// Cancel all tasks
		if (delays != null && delays.size() > 0)
			for (Future<?> delay : delays)
				delay.cancel(false);
		
		// Clear all announcements
		announcements.clear();
		
		// And load again all announcements
		load();
	}
	
	/**
	 * Load the announcements system
	 */
	private void load()
	{
		announcements = new FastSet<Announcement>(getDAO().getAnnouncements()).shared();
		
		for (final Announcement announce : announcements)
		{
			delays.add(ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
			{
				@Override
				public void run()
				{
					for(final Player player : World.getInstance().getAllPlayers())
					{
						if (announce.getFaction().equalsIgnoreCase("ALL"))
							if (announce.getChatType() == ChatType.SHOUT || announce.getChatType() == ChatType.GROUP_LEADER)
								PacketSendUtility.sendPacket(player, new SM_MESSAGE(1, "Automatic Announce", announce.getAnnounce(), announce.getChatType()));
							else
								PacketSendUtility.sendPacket(player, new SM_MESSAGE(1, "Automatic Announce", "Automatic Announce: " + announce.getAnnounce(), announce.getChatType()));
						else if (announce.getFactionEnum() == player.getCommonData().getRace())
							if (announce.getChatType() == ChatType.SHOUT || announce.getChatType() == ChatType.GROUP_LEADER)
								PacketSendUtility.sendPacket(player, new SM_MESSAGE(1, (announce.getFaction().equalsIgnoreCase("ELYOS") ? "Elyos" : "Asmodian") + " Automatic Announce", announce.getAnnounce(), announce.getChatType()));
							else
								PacketSendUtility.sendPacket(player, new SM_MESSAGE(1, (announce.getFaction().equalsIgnoreCase("ELYOS") ? "Elyos" : "Asmodian") + " Automatic Announce", (announce.getFaction().equalsIgnoreCase("ELYOS") ? "Elyos" : "Asmodian") + " Automatic Announce: " + announce.getAnnounce(), announce.getChatType()));
					}
				}
			}, announce.getDelay() * 1000, announce.getDelay() * 1000));
		}
		
		log.info("Loaded " + announcements.size() + " announcements");
	}
	
	public void addAnnouncement(Announcement announce)
	{
		getDAO().addAnnouncement(announce);
	}
	
	public boolean delAnnouncement(final int idAnnounce)
	{
		return getDAO().delAnnouncement(idAnnounce);
	}
	
	public Set<Announcement> getAnnouncements()
	{
		return getDAO().getAnnouncements();
	}

	/**
	 * Retuns {@link com.aionemu.loginserver.dao.AnnouncementDAO} , just a shortcut
	 * 
	 * @return {@link com.aionemu.loginserver.dao.AnnouncementDAO}
	 */
	private AnnouncementsDAO getDAO()
	{
		return DAOManager.getDAO(AnnouncementsDAO.class);
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AnnouncementService instance = new AnnouncementService();
	}
}
