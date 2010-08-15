/*
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
package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.TaskFromDBDAO;
import com.aionemu.gameserver.model.tasks.TaskFromDB;

/**
 * Tasks from DB
 * 
 * @author Divinity
 * 
 * Based on L2J Emulator Global Tasks System
 * @author From L2J : Layane
 * 
 */
public class MySQL5TaskFromDBDAO extends TaskFromDBDAO
{
	/**
	 * Logger for this class.
	 */
	private static final Logger	log					= Logger.getLogger(MySQL5TaskFromDBDAO.class);
	
	private static final String SELECT_ALL_QUERY	= "SELECT * FROM tasks ORDER BY id";
	private static final String UPDATE_QUERY		= "UPDATE tasks SET last_activation = ? WHERE id = ?";
	
	@Override
	public ArrayList<TaskFromDB> getAllTasks()
	{
		final ArrayList<TaskFromDB> result = new ArrayList<TaskFromDB>();
		
		Connection con = null;
		
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(SELECT_ALL_QUERY);
			
			ResultSet rset = stmt.executeQuery();
			
			while(rset.next())
				result.add(new TaskFromDB(
					rset.getInt("id"), 
					rset.getString("task"), 
					rset.getString("type"), 
					rset.getTimestamp("last_activation"), 
					rset.getString("startTime"), 
					rset.getInt("delay"), 
					rset.getString("param")
				));

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
				
		return result;
	}
	
	@Override
	public void setLastActivation(final int id)
	{
		Connection con = null;
		
		try
		{
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY);
			
			stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			stmt.setInt(2, id);
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
	public boolean supports(String s, int i, int i1)
	{
		return MySQL5DAOUtils.supports(s, i, i1);
	}
}