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
package com.aionemu.gameserver.model.legion;

import com.aionemu.gameserver.model.gameobjects.player.Storage;
import com.aionemu.gameserver.model.gameobjects.player.StorageType;

/**
 * @author Simple
 */
public class LegionWarehouse extends Storage
{
	private Legion	legion;

	public LegionWarehouse(Legion legion)
	{
		super(StorageType.LEGION_WAREHOUSE);
		this.legion = legion;
		this.setLimit(legion.getWarehouseSlots());
	}

	/**
	 * @return the legion
	 */
	public Legion getLegion()
	{
		return this.legion;
	}

	/**
	 * @param legion
	 *            the legion to set
	 */
	public void setOwnerLegion(Legion legion)
	{
		this.legion = legion;
	}
	
	
}
