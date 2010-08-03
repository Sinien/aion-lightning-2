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
package com.aionemu.gameserver.ai.npcai;

import com.aionemu.gameserver.ai.events.EventHandlers;
import com.aionemu.gameserver.ai.state.StateHandlers;

/**
 * @author ATracer
 *
 */
public class MonsterAi extends NpcAi
{
	public MonsterAi()
	{
		super();
		this.addEventHandler(EventHandlers.ATTACKED_EH.getHandler());
		this.addEventHandler(EventHandlers.TIREDATTACKING_EH.getHandler());
		this.addEventHandler(EventHandlers.MOST_HATED_CHANGED_EH.getHandler());
		this.addEventHandler(EventHandlers.BACKHOME_EH.getHandler());
		this.addEventHandler(EventHandlers.RESTOREDHEALTH_EH.getHandler());
		
		this.addStateHandler(StateHandlers.MOVINGTOHOME_SH.getHandler());
		this.addStateHandler(StateHandlers.NONE_MONSTER_SH.getHandler());
		this.addStateHandler(StateHandlers.ATTACKING_SH.getHandler());
		this.addStateHandler(StateHandlers.THINKING_SH.getHandler());
		this.addStateHandler(StateHandlers.RESTING_SH.getHandler());
	}
}