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

import com.aionemu.gameserver.ai.AI;
import com.aionemu.gameserver.ai.events.Event;
import com.aionemu.gameserver.ai.events.EventHandlers;
import com.aionemu.gameserver.ai.events.handler.EventHandler;
import com.aionemu.gameserver.ai.state.StateHandlers;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author ATracer
 *
 */
public class NpcAi extends AI<Npc>
{
	public NpcAi()
	{
		/**
		 * Event Handlers
		 */
		this.addEventHandler(EventHandlers.NOTHINGTODO_EH.getHandler());
		this.addEventHandler(EventHandlers.RESPAWNED_EH.getHandler());
		this.addEventHandler(EventHandlers.DIED_EH.getHandler());
		this.addEventHandler(EventHandlers.DESPAWN_EH.getHandler());
		this.addEventHandler(EventHandlers.DAYTIMECHANGE_EH.getHandler());
		this.addEventHandler(EventHandlers.TALK_EH.getHandler());
		
		/**
		 * State Handlers
		 */
		this.addStateHandler(StateHandlers.ACTIVE_NPC_SH.getHandler());
		this.addStateHandler(StateHandlers.TALKING_SH.getHandler());
	}

	@Override
	public void handleEvent(Event event)
	{
		super.handleEvent(event);
		
		//allow only handling event Event.DIED in dead stats
		//probably i need to define rules for which events could be handled in which state
		if(event != Event.DIED && owner.getLifeStats().isAlreadyDead())
			return;
		
		EventHandler eventHandler = eventHandlers.get(event);
		if(eventHandler != null)
			eventHandler.handleEvent(event, this);
	}
}
