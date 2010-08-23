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
package com.aionemu.commons.log4j.filters;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Log4J filter that looks if there is chat log present in the logging event and accepts event if present. Otherwise it
 * blocks filtring.
 * 
 * @author Divinity
 */
public final class ChatLogFilter extends Filter
{
	/*
	 * (non-Javadoc)
	 * @see org.apache.log4j.spi.Filter#decide(org.apache.log4j.spi.LoggingEvent)
	 */
	@Override
	public final int decide(LoggingEvent loggingEvent)
	{
		Object message = loggingEvent.getMessage();

		if(((String) message).startsWith("[MESSAGE]"))
		{
			return ACCEPT;
		}

		return DENY;
	}
}
