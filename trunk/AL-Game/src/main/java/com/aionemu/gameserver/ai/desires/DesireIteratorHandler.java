/*
 * This file is part of aion-emu <aion-emu.com>.
 *
 * aion-emu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aion-emu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with aion-emu.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.ai.desires;

import java.util.Iterator;

/**
 * This class is designed to be a helper class for desires.<br>
 * Direct access to desire list is not allowed, however this interface can be used for iteration.<br>
 * <br>
 * 
 * @author SoulKeeper
 * @see com.aionemu.gameserver.ai.desires.DesireIteratorFilter
 * @see com.aionemu.gameserver.ai.desires.DesireQueue#iterateDesires(DesireIteratorHandler, DesireIteratorFilter[])
 */
public interface DesireIteratorHandler
{
	/**
	 * This method is called each time for every desire that is in the queue.<br>
	 * Remove of desire must be handeled by <b>iterator.remove();</b><br>
	 * <br> {@link java.util.ConcurrentModificationException} will be thrown by
	 * {@link com.aionemu.gameserver.ai.desires.DesireQueue#iterateDesires(DesireIteratorHandler, DesireIteratorFilter[])}
	 * if any of the following methods will be called from here:
	 * <ul>
	 * <li>{@link com.aionemu.gameserver.ai.desires.DesireQueue#addDesire(Desire)}</li>
	 * <li>{@link com.aionemu.gameserver.ai.desires.DesireQueue#poll()}</li>
	 * <li>{@link com.aionemu.gameserver.ai.desires.DesireQueue#removeDesire(Desire)}</li>
	 * </ul>
	 * <p/>
	 * However {@link com.aionemu.gameserver.ai.desires.DesireQueue#clear()} can be called.
	 * 
	 * @param desire
	 *            current element of iteration
	 * @param iterator
	 *            iterator object
	 */
	public void next(Desire desire, Iterator<Desire> iterator);
}
