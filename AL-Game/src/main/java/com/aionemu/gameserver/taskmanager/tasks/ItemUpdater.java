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
package com.aionemu.gameserver.taskmanager.tasks;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.TaskManagerConfig;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.ItemStoneListDAO;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.taskmanager.AbstractFIFOPeriodicTaskManager;

/**
 * @author Mr. Poke
 *
 */
public class ItemUpdater extends AbstractFIFOPeriodicTaskManager<Item>
{

	private static final class SingletonHolder
	{
		private static final ItemUpdater	INSTANCE	= new ItemUpdater();
	}

	public static ItemUpdater getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	/**
	 * @param period
	 */
	public ItemUpdater()
	{
		super(TaskManagerConfig.ITEMS * 1000);
	}

	/* (non-Javadoc)
	 * @see com.aionemu.gameserver.taskmanager.AbstractFIFOPeriodicTaskManager#callTask(java.lang.Object)
	 */
	@Override
	protected void callTask(Item item)
	{
		DAOManager.getDAO(InventoryDAO.class).store(item);
		DAOManager.getDAO(ItemStoneListDAO.class).store(item.getGodStone());
		DAOManager.getDAO(ItemStoneListDAO.class).store(item.getItemStones());
	}

	/* (non-Javadoc)
	 * @see com.aionemu.gameserver.taskmanager.AbstractFIFOPeriodicTaskManager#getCalledMethodName()
	 */
	@Override
	protected String getCalledMethodName()
	{
		return "ItemUpdater()";
	}

}
