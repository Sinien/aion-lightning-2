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

import org.apache.log4j.Logger;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.PlayerLifeStatsDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.stats.PlayerLifeStats;

/**
 * @author Mr. Poke
 *
 */
public class MySQL5PlayerLifeStatsDAO extends PlayerLifeStatsDAO
{

	/** Logger */
	private static final Logger					log					= Logger.getLogger(MySQL5PlayerLifeStatsDAO.class);

	public static final String INSERT_QUERY = "INSERT INTO `player_life_stats` (`player_id`, `hp`, `mp`, `fp`) VALUES (?,?,?,?)";
	public static final String DELETE_QUERY = "DELETE FROM `player_life_stats` WHERE `player_id`=?";
	public static final String SELECT_QUERY = "SELECT `hp`, `mp`, `fp` FROM `player_life_stats` WHERE `player_id`=?";
	public static final String UPDATE_QUERY = "UPDATE player_life_stats set `hp`=?, `mp`=?, `fp`=? WHERE `player_id`=?";

	/* (non-Javadoc)
	 * @see com.aionemu.gameserver.dao.PlayerLifeStatsDAO#loadPlayerLifeStat(com.aionemu.gameserver.model.gameobjects.player.Player)
	 */
	@Override
	public void loadPlayerLifeStat(final Player player)
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(SELECT_QUERY);
			stmt.setInt(1, player.getObjectId());
			ResultSet rset = stmt.executeQuery();
			if(rset.next())
			{
				PlayerLifeStats lifeStats = player.getLifeStats();
				lifeStats.setCurrentHp(rset.getInt("hp"));
				lifeStats.setCurrentMp(rset.getInt("mp"));
				lifeStats.setCurrentFp(rset.getInt("fp"));
			}
			else
				insertPlayerLifeStat(player);
			rset.close();
			stmt.close();
		}
		catch (Exception e)
		{
			log.fatal("Could not restore PlayerLifeStat data for playerObjId: " + player.getObjectId() + " from DB: "+e.getMessage(), e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}
	}

	/* (non-Javadoc)
	 * @see com.aionemu.gameserver.dao.PlayerLifeStatsDAO#storePlayerLifeStat(com.aionemu.gameserver.model.gameobjects.player.Player)
	 */
	@Override
	public void insertPlayerLifeStat(final Player player)
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(INSERT_QUERY);
			stmt.setInt(1, player.getObjectId());
			stmt.setInt(2, player.getLifeStats().getCurrentHp());
			stmt.setInt(3, player.getLifeStats().getCurrentMp());
			stmt.setInt(4, player.getLifeStats().getCurrentFp());
			stmt.execute();
			stmt.close();
		}
		catch (Exception e)
		{
			log.fatal("Could not store PlayerLifeStat data for player " + player.getObjectId() + " from DB: "+e.getMessage(), e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}
	}


	/* (non-Javadoc)
	 * @see com.aionemu.gameserver.dao.PlayerLifeStatsDAO#deletePlayerLifeStat(com.aionemu.gameserver.model.gameobjects.player.Player)
	 */
	@Override
	public void deletePlayerLifeStat(final int playerId)
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(DELETE_QUERY);
			stmt.setInt(1, playerId);
			stmt.execute();
			stmt.close();
		}
		catch (Exception e)
		{
			log.fatal("Could not delete PlayerLifeStat data for player " + playerId + " from DB: "+e.getMessage(), e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}
	}

	/* (non-Javadoc)
	 * @see com.aionemu.gameserver.dao.PlayerLifeStatsDAO#updatePlayerLifeStat(com.aionemu.gameserver.model.gameobjects.player.Player)
	 */
	@Override
	public void updatePlayerLifeStat(final Player player)
	{
		Connection con = null;
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY);
			stmt.setInt(1, player.getLifeStats().getCurrentHp());
			stmt.setInt(2, player.getLifeStats().getCurrentMp());
			stmt.setInt(3, player.getLifeStats().getCurrentFp());
			stmt.setInt(4, player.getObjectId());
			stmt.execute();
			stmt.close();
		}
		catch (Exception e)
		{
			log.fatal("Could not update PlayerLifeStat data for player " + player.getObjectId() + " from DB: "+e.getMessage(), e);
		}
		finally
		{
			DatabaseFactory.close(con);
		}
	}

	/* (non-Javadoc)
	 * @see com.aionemu.commons.database.dao.DAO#supports(java.lang.String, int, int)
	 */
	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion)
	{
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}
}
