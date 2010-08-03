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
package com.aionemu.gameserver.ai.events;

/**
 * @author ATracer
 *
 */
public enum Event
{
	/**
	 * This event is received on each enemy attack
	 */
	ATTACKED,
	/**
	 * Target is too far or long time passed since last attak
	 */
	TIRED_ATTACKING_TARGET,
	/**
	 * During attack most hated creature changed from current target
	 */
	MOST_HATED_CHANGED,
	/**
	 * In active state there is nothing to do
	 */
	NOTHING_TODO,
	/**
	 * Npc is far from spawn point
	 */
	FAR_FROM_HOME,
	/**
	 * Npc returned to spawn point
	 */
	BACK_HOME,
	/**
	 * Npc restored health fully (after returning to home)
	 */
	RESTORED_HEALTH,
	/**
	 * Npc sees another player
	 */
	SEE_PLAYER,
	/**
	 * Player removed from knownlist
	 */
	NOT_SEE_PLAYER,
	/**
	 * Any creature is in the visible radius
	 */
	SEE_CREATURE,
	/**
	 * Creature removed from knownlist
	 */
	NOT_SEE_CREATURE,
	/**
	 * Talk request
	 */
	TALK,
	/**
	 * Npc is respawned
	 */
	RESPAWNED,
	/**
	 * Creature died
	 */
	DIED,
	/**
	 * DayTime changed
	 */
	DAYTIME_CHANGE,
	/**
	 * Despawn service was called
	 */
	DESPAWN
}
