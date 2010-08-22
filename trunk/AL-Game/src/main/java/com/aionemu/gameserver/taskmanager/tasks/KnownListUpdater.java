/*
 * This file is part of aion-lightning <aion-lightning.com>.
 *
 *  aion-lightning is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-lightning is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-lightning.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.taskmanager.tasks;

import com.aionemu.gameserver.configs.main.TaskManagerConfig;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.taskmanager.AbstractPeriodicTaskManager;
import com.aionemu.gameserver.world.World;

/**
 * @author lord_rex 
 * 		based on l2j-free engines.
 */
public final class KnownListUpdater extends AbstractPeriodicTaskManager
{
	private static final class SingletonHolder
	{
		private static final KnownListUpdater	INSTANCE	= new KnownListUpdater();
	}

	public static KnownListUpdater getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	public KnownListUpdater()
	{
		super(TaskManagerConfig.KNOWNLIST_CLEAR * 60 * 1000);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aionemu.gameserver.taskmanager.AbstractPeriodicTaskManager#run()
	 */
	@Override
	public void run()
	{
		for(AionObject object : World.getInstance().getAllObjects())
		{
			if(object instanceof VisibleObject)
			{
				((VisibleObject) object).getKnownList().clearKnownList();
				((VisibleObject) object).getKnownList().updateKnownList();
			}
		}
	}
}
