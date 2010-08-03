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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.dao.PlayerEffectsDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
 *
 */
public class MySQL5PlayerEffectsDAO extends PlayerEffectsDAO
{
	public static final String INSERT_QUERY = "INSERT INTO `player_effects` (`player_id`, `skill_id`, `skill_lvl`, `current_time`, `reuse_delay`) VALUES (?,?,?,?,?)";
	public static final String DELETE_QUERY = "DELETE FROM `player_effects` WHERE `player_id`=?";
	public static final String SELECT_QUERY = "SELECT `skill_id`, `skill_lvl`, `current_time`, `reuse_delay` FROM `player_effects` WHERE `player_id`=?";

	
	@Override
	public void loadPlayerEffects(final Player player)
	{
		DB.select(SELECT_QUERY, new ParamReadStH()
		{
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException
			{
				stmt.setInt(1, player.getObjectId());
			}

			@Override
			public void handleRead(ResultSet rset) throws SQLException
			{
				while(rset.next())
				{
					int skillId = rset.getInt("skill_id");
					int skillLvl = rset.getInt("skill_lvl");
					int currentTime = rset.getInt("current_time");
					long reuseDelay = rset.getLong("reuse_delay");
					
					if(currentTime > 0)
						player.getEffectController().addSavedEffect(skillId, skillLvl, currentTime);
					
					if(reuseDelay > System.currentTimeMillis())
						player.setSkillCoolDown(skillId, reuseDelay);
					
				}
			}
		});
		player.getEffectController().broadCastEffects();
	}

	@Override
	public void storePlayerEffects(final Player player)
	{
		deletePlayerEffects(player);
		Iterator<Effect> iterator = player.getEffectController().iterator();		
		
		while(iterator.hasNext())
		{
			final Effect effect = iterator.next();
			final int elapsedTime = effect.getElapsedTime();
			
			if(elapsedTime < 60000)
				continue;

			DB.insertUpdate(INSERT_QUERY, new IUStH() {
				@Override
				public void handleInsertUpdate(PreparedStatement stmt) throws SQLException
				{
					stmt.setInt(1, player.getObjectId());
					stmt.setInt(2, effect.getSkillId());
					stmt.setInt(3, effect.getSkillLevel());
					stmt.setInt(4, effect.getCurrentTime());
					
					long reuseTime = player.getSkillCoolDown(effect.getSkillId());
					player.removeSkillCoolDown(effect.getSkillId());
					
					stmt.setLong(5, reuseTime);
					stmt.execute();
				}
			});
		}
		
		final Map<Integer, Long> cooldowns = player.getSkillCoolDowns();
		if(cooldowns != null)
		{
			for(Map.Entry<Integer, Long> entry : cooldowns.entrySet())
			{
				final int skillId = entry.getKey();
				final long reuseTime = entry.getValue();
				if(reuseTime - System.currentTimeMillis() < 60000)
					continue;
				
				DB.insertUpdate(INSERT_QUERY, new IUStH() {
					@Override
					public void handleInsertUpdate(PreparedStatement stmt) throws SQLException
					{
						stmt.setInt(1, player.getObjectId());
						stmt.setInt(2, skillId);
						stmt.setInt(3, 0);
						stmt.setInt(4, 0);											
						stmt.setLong(5, reuseTime);
						stmt.execute();
					}
				});
			}
		}
	}
	
	private void deletePlayerEffects(final Player player)
	{
		DB.insertUpdate(DELETE_QUERY, new IUStH()
		{
			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException
			{
				stmt.setInt(1, player.getObjectId());
				stmt.execute();
			}
		});
	}

	@Override
	public boolean supports(String arg0, int arg1, int arg2)
	{
		return MySQL5DAOUtils.supports(arg0, arg1, arg2);
	}
}
