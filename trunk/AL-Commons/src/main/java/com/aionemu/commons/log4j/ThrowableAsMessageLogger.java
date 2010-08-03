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
package com.aionemu.commons.log4j;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * This class is designed to handle situations when log4J was called like:<br>
 * 
 * <pre>
 * org.apache.log4j.Logger#log(Throwable)
 * </pre>
 * 
 * <p/>
 * In such cases this logger will take message from throwable and set it as message. Throwable will be threated as real
 * throwable, so no stacktraces would be lost.
 * 
 * @author SoulKeeper
 */
public class ThrowableAsMessageLogger extends Logger
{
	/**
	 * Creates new instance of this logger
	 * 
	 * @param name
	 *            logger's name
	 */
	protected ThrowableAsMessageLogger(String name)
	{
		super(name);
	}

	/**
	 * This method checks if message is instance of throwbale and throwable is null. If it is so it will move message to
	 * throwable and set localized message of throwable as message of the log record
	 * 
	 * @param fqcn
	 *            fully qualified class name, it would be used to get the line of call
	 * @param level
	 *            level of log record
	 * @param message
	 *            message of log record
	 * @param t
	 *            throwable, if any present
	 */
	@Override
	protected void forcedLog(String fqcn, Priority level, Object message, Throwable t)
	{

		if(message instanceof Throwable && t == null)
		{
			t = (Throwable) message;
			message = t.getLocalizedMessage();
		}

		super.forcedLog(fqcn, level, message, t);
	}
}
