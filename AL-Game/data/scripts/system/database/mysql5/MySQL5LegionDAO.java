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
package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.LegionDAO;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.StorageType;
import com.aionemu.gameserver.model.legion.Legion;
import com.aionemu.gameserver.model.legion.LegionEmblem;
import com.aionemu.gameserver.model.legion.LegionHistory;
import com.aionemu.gameserver.model.legion.LegionHistoryType;
import com.aionemu.gameserver.model.legion.LegionWarehouse;

/**
 * Class that that is responsible for loading/storing {@link com.aionemu.gameserver.model.legion.Legion} object from
 * MySQL 5.
 * 
 * @author Simple
 */
public class MySQL5LegionDAO extends LegionDAO
{
	/** Logger */
	private static final Logger	log								= Logger.getLogger(MySQL5LegionDAO.class);

	/** Legion Queries */
	private static final String	INSERT_LEGION_QUERY				= "INSERT INTO legions(id, `name`) VALUES (?, ?)";
	private static final String	SELECT_LEGION_QUERY1			= "SELECT * FROM legions WHERE id=?";
	private static final String	SELECT_LEGION_QUERY2			= "SELECT * FROM legions WHERE name=?";
	private static final String	DELETE_LEGION_QUERY				= "DELETE FROM legions WHERE id = ?";
	private static final String	UPDATE_LEGION_QUERY				= "UPDATE legions SET name=?, level=?, contribution_points=?, legionar_permission2=?, centurion_permission1=?, centurion_permission2=?, disband_time=? WHERE id=?";

	/** Legion Ranking Queries **/
	private static final String	SELECT_LEGIONRANKING_QUERY		= "SELECT id, contribution_points FROM legions ORDER BY contribution_points DESC;";

	/** Announcement Queries **/
	private static final String	INSERT_ANNOUNCEMENT_QUERY		= "INSERT INTO legion_announcement_list(`legion_id`, `announcement`, `date`) VALUES (?, ?, ?)";
	private static final String	SELECT_ANNOUNCEMENTLIST_QUERY	= "SELECT * FROM legion_announcement_list WHERE legion_id=? ORDER BY date ASC LIMIT 0,7;";
	private static final String	DELETE_ANNOUNCEMENT_QUERY		= "DELETE FROM legion_announcement_list WHERE legion_id = ? AND date = ?";

	/** Emblem Queries **/
	private static final String	INSERT_EMBLEM_QUERY				= "INSERT INTO legion_emblems(legion_id, emblem_id, color_r, color_g, color_b) VALUES (?, ?, ?, ?, ?)";
	private static final String	UPDATE_EMBLEM_QUERY				= "UPDATE legion_emblems SET emblem_id=?, color_r=?, color_g=?, color_b=? WHERE legion_id=?";
	private static final String	SELECT_EMBLEM_QUERY				= "SELECT * FROM legion_emblems WHERE legion_id=?";

	/** Storage Queries **/
	private static final String	SELECT_STORAGE_QUERY			= "SELECT `itemUniqueId`, `itemId`, `itemCount`, `itemColor`, `isEquiped`, `slot`, `enchant`, `itemSkin`, `fusionedItem` FROM `inventory` WHERE `itemOwner`=? AND `itemLocation`=? AND `isEquiped`=?";

	/** History Queries **/
	private static final String	INSERT_HISTORY_QUERY			= "INSERT INTO legion_history(`legion_id`, `date`, `history_type`, `name`) VALUES (?, ?, ?, ?)";
	private static final String	SELECT_HISTORY_QUERY			= "SELECT * FROM `legion_history` WHERE legion_id=? ORDER BY date ASC;";

	private static final String SELECT_QUERY 					= "SELECT id FROM legions";
	
	@Override
	public boolean isNameUsed(final String name)
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement("SELECT count(id) as cnt FROM legions WHERE ? = legions.name");
			stmt.setString(1, name);
			ResultSet rset = stmt.executeQuery();
			rset.next();
			return rset.getInt("cnt") > 0;
		}
		catch(SQLException e)
		{
			log.error("Can't check if name " + name + ", is used, returning possitive result", e);
			return true;
		}
		finally
		{
			DatabaseFactory.close(con);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean saveNewLegion(final Legion legion)
	{
		Connection con = null;
		
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(INSERT_LEGION_QUERY);
			
			log.debug("[DAO: MySQL5LegionDAO] saving new legion: " + legion.getLegionId() + " "
				+ legion.getLegionName());

			stmt.setInt(1, legion.getLegionId());
			stmt.setString(2, legion.getLegionName());
			stmt.execute();
		}
		catch(SQLException e)
		{
			log.error(e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}
		
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storeLegion(final Legion legion)
	{
		Connection con = null;
		
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(UPDATE_LEGION_QUERY);
			
			log.debug("[DAO: MySQL5LegionDAO] storing player " + legion.getLegionId() + " "
				+ legion.getLegionName());

			stmt.setString(1, legion.getLegionName());
			stmt.setInt(2, legion.getLegionLevel());
			stmt.setInt(3, legion.getContributionPoints());
			stmt.setInt(4, legion.getLegionarPermission2());
			stmt.setInt(5, legion.getCenturionPermission1());
			stmt.setInt(6, legion.getCenturionPermission2());
			stmt.setInt(7, legion.getDisbandTime());
			stmt.setInt(8, legion.getLegionId());
			stmt.execute();
		}
		catch(SQLException e)
		{
			log.error(e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Legion loadLegion(final String legionName)
	{
		final Legion legion = new Legion();
		
		Connection con = null;
		
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(SELECT_LEGION_QUERY2);
			
			stmt.setString(1, legionName);
			
			ResultSet rset = stmt.executeQuery();

			while(rset.next())
			{
				legion.setLegionName(legionName);
				legion.setLegionId(rset.getInt("id"));
				legion.setLegionLevel(rset.getInt("level"));
				legion.addContributionPoints(rset.getInt("contribution_points"));

				legion.setLegionPermissions(
					rset.getInt("legionar_permission2"), 
					rset.getInt("centurion_permission1"), 
					rset.getInt("centurion_permission2")
				);

				legion.setDisbandTime(rset.getInt("disband_time"));
			}
			
			rset.close();
			stmt.close();
		}
		catch(SQLException e)
		{
			log.error(e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}

		log.debug("[MySQL5LegionDAO] Loaded " + legion.getLegionId() + " legion.");

		return (legion.getLegionId() != 0) ? legion : null;
	}

	@Override
	public Legion loadLegion(final int legionId)
	{
		final Legion legion = new Legion();
		
		Connection con = null;
		
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(SELECT_LEGION_QUERY1);
			
			stmt.setInt(1, legionId);
			
			ResultSet rset = stmt.executeQuery();

			while(rset.next())
			{
				legion.setLegionId(legionId);
				legion.setLegionName(rset.getString("name"));
				legion.setLegionLevel(rset.getInt("level"));
				legion.addContributionPoints(rset.getInt("contribution_points"));

				legion.setLegionPermissions(
					rset.getInt("legionar_permission2"), 
					rset.getInt("centurion_permission1"), 
					rset.getInt("centurion_permission2")
				);

				legion.setDisbandTime(rset.getInt("disband_time"));
			}
			
			rset.close();
			stmt.close();
		}
		catch(SQLException e)
		{
			log.error(e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}

		log.debug("[MySQL5LegionDAO] Loaded " + legion.getLegionId() + " legion.");

		return (legion.getLegionName() != "") ? legion : null;
	}

	@Override
	public void deleteLegion(int legionId)
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(DELETE_LEGION_QUERY);
			
			stmt.setInt(1, legionId);
			stmt.execute();
		}
		catch(SQLException e)
		{
			log.error("Some crap, can't set int parameter to PreparedStatement", e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}
	}

	@Override
	public int[] getUsedIDs()
	{
		Connection con = null;

		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(SELECT_QUERY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rset = stmt.executeQuery();
			
			rset.last();
			int count = rset.getRow();
			rset.beforeFirst();
			int[] ids = new int[count];
			for(int i = 0; i < count; i++)
			{
				rset.next();
				ids[i] = rset.getInt("id");
			}
			
			rset.close();
			stmt.close();
			
			return ids;
		}
		catch(SQLException e)
		{
			log.error("Can't get list of id's from legions table", e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}

		return new int[0];
	}

	@Override
	public boolean supports(String s, int i, int i1)
	{
		return MySQL5DAOUtils.supports(s, i, i1);
	}

	@Override
	public TreeMap<Timestamp, String> loadAnnouncementList(final int legionId)
	{
		final TreeMap<Timestamp, String> announcementList = new TreeMap<Timestamp, String>();
		
		Connection con = null;
		
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(SELECT_ANNOUNCEMENTLIST_QUERY);
			
			stmt.setInt(1, legionId);
			
			ResultSet rset = stmt.executeQuery();

			while(rset.next())
			{
				String message = rset.getString("announcement");
				Timestamp date = rset.getTimestamp("date");

				announcementList.put(date, message);
			}
			
			rset.close();
			stmt.close();
		}
		catch(SQLException e)
		{
			log.error(e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}

		log.debug("[MySQL5LegionDAO] Loaded announcementList " + legionId + " legion.");

		return announcementList;
	}

	@Override
	public boolean saveNewAnnouncement(final int legionId, final Timestamp currentTime, final String message)
	{	
		Connection con = null;
		
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(INSERT_ANNOUNCEMENT_QUERY);
			
			log.debug("[DAO: MySQL5LegionDAO] saving new announcement.");

			stmt.setInt(1, legionId);
			stmt.setString(2, message);
			stmt.setTimestamp(3, currentTime);
			stmt.execute();
		}
		catch(SQLException e)
		{
			log.error(e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}
		
		return true;
	}

	@Override
	public void removeAnnouncement(int legionId, Timestamp unixTime)
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(DELETE_ANNOUNCEMENT_QUERY);
			
			stmt.setInt(1, legionId);
			stmt.setTimestamp(2, unixTime);
			stmt.execute();
		}
		catch(SQLException e)
		{
			log.error("Some crap, can't set int parameter to PreparedStatement", e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}
	}

	@Override
	public void storeLegionEmblem(final int legionId, final LegionEmblem legionEmblem)
	{
		switch(legionEmblem.getPersistentState())
		{
			case UPDATE_REQUIRED:
				updateLegionEmblem(legionId, legionEmblem);
				break;
			case NEW:
				createLegionEmblem(legionId, legionEmblem);
				break;
		}
		legionEmblem.setPersistentState(PersistentState.UPDATED);
	}
	
	/**
	 * 
	 * @param legionId
	 * @param legionEmblem
	 * @return
	 */
	private void createLegionEmblem(final int legionId, final LegionEmblem legionEmblem)
	{
		Connection con = null;
		
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(INSERT_EMBLEM_QUERY);
			
			stmt.setInt(1, legionId);
			stmt.setInt(2, legionEmblem.getEmblemId());
			stmt.setInt(3, legionEmblem.getColor_r());
			stmt.setInt(4, legionEmblem.getColor_g());
			stmt.setInt(5, legionEmblem.getColor_b());
			stmt.execute();
		}
		catch(SQLException e)
		{
			log.error(e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}
	}
	
	/**
	 * 
	 * @param legionId
	 * @param legionEmblem
	 */
	private void updateLegionEmblem(final int legionId, final LegionEmblem legionEmblem)
	{
		Connection con = null;
		
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(UPDATE_EMBLEM_QUERY);
			
			stmt.setInt(1, legionEmblem.getEmblemId());
			stmt.setInt(2, legionEmblem.getColor_r());
			stmt.setInt(3, legionEmblem.getColor_g());
			stmt.setInt(4, legionEmblem.getColor_b());
			stmt.setInt(5, legionId);
			stmt.execute();
		}
		catch(SQLException e)
		{
			log.error(e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}
	}

	@Override
	public LegionEmblem loadLegionEmblem(final int legionId)
	{
		final LegionEmblem legionEmblem = new LegionEmblem();
		
		Connection con = null;
		
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(SELECT_EMBLEM_QUERY);
			
			stmt.setInt(1, legionId);
			
			ResultSet rset = stmt.executeQuery();

			while(rset.next())
			{
				legionEmblem.setEmblem(
					rset.getInt("emblem_id"), 
					rset.getInt("color_r"), 
					rset.getInt("color_g"), 
					rset.getInt("color_b")
				);
			}
			
			rset.close();
			stmt.close();
		}
		catch(SQLException e)
		{
			log.error(e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}

		legionEmblem.setPersistentState(PersistentState.UPDATED);

		return legionEmblem;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LegionWarehouse loadLegionStorage(Legion legion)
	{
		final LegionWarehouse inventory = new LegionWarehouse(legion);
		final int legionId = legion.getLegionId();
		final int storage = StorageType.LEGION_WAREHOUSE.getId();
		final int equipped = 0;
		
		Connection con = null;
		
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(SELECT_STORAGE_QUERY);
			
			stmt.setInt(1, legionId);
			stmt.setInt(2, storage);
			stmt.setInt(3, equipped);
			
			ResultSet rset = stmt.executeQuery();
			
			while(rset.next())
			{
				int itemUniqueId = rset.getInt("itemUniqueId");
				int itemId = rset.getInt("itemId");
				int itemCount = rset.getInt("itemCount");
				int itemColor = rset.getInt("itemColor");
				int isEquiped = rset.getInt("isEquiped");
				int slot = rset.getInt("slot");
				int enchant = rset.getInt("enchant");
				int itemSkin = rset.getInt("itemSkin");
				int fusionedItem = rset.getInt("fusionedItem");
				Item item = new Item(legionId, itemUniqueId, itemId, itemCount, itemColor, isEquiped == 1, false, slot, storage, enchant, itemSkin,fusionedItem);
				item.setPersistentState(PersistentState.UPDATED);
				inventory.onLoadHandler(item);
			}
			
			rset.close();
			stmt.close();
		}
		catch(SQLException e)
		{
			log.error(e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}

		return inventory;
	}

	@Override
	public HashMap<Integer, Integer> loadLegionRanking()
	{
		final HashMap<Integer, Integer> legionRanking = new HashMap<Integer, Integer>();
		
		Connection con = null;
		
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(SELECT_LEGIONRANKING_QUERY);
			ResultSet rset = stmt.executeQuery();
			
			int i = 1;
			while(rset.next())
			{
				if(rset.getInt("contribution_points") > 0)
				{
					legionRanking.put(rset.getInt("id"), i);
					i++;
				}
				else
					legionRanking.put(rset.getInt("id"), 0);
			}

			rset.close();
			stmt.close();
		}
		catch(SQLException e)
		{
			log.error(e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}

		return legionRanking;
	}

	@Override
	public void loadLegionHistory(final Legion legion)
	{
		final Collection<LegionHistory> history = legion.getLegionHistory();
		
		Connection con = null;
		
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(SELECT_HISTORY_QUERY);
			
			stmt.setInt(1, legion.getLegionId());
			
			ResultSet rset = stmt.executeQuery();
			
			while(rset.next())
			{
				history.add(new LegionHistory(LegionHistoryType.valueOf(
					rset.getString("history_type")),
					rset.getString("name"), 
					rset.getTimestamp("date")
				));
			}

			rset.close();
			stmt.close();
		}
		catch(SQLException e)
		{
			log.error(e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}
	}

	@Override
	public boolean saveNewLegionHistory(final int legionId, final LegionHistory legionHistory)
	{
		Connection con = null;
		
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(INSERT_HISTORY_QUERY);
			
			stmt.setInt(1, legionId);
			stmt.setTimestamp(2, legionHistory.getTime());
			stmt.setString(3, legionHistory.getLegionHistoryType().toString());
			stmt.setString(4, legionHistory.getName());
			stmt.execute();
		}
		catch(SQLException e)
		{
			log.error(e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}
		
		return true;
	}
}
