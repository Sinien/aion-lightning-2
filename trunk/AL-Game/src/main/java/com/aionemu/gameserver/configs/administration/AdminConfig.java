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
package com.aionemu.gameserver.configs.administration;

import com.aionemu.commons.configuration.Property;

/**
 * @author ATracer
 */
public class AdminConfig
{
	@Property(key = "administration.flight.unlimited", defaultValue = "3")
	public static int	GM_FLIGHT_UNLIMITED;

	@Property(key = "administration.command.add", defaultValue = "3")
	public static int	COMMAND_ADD;

	@Property(key = "administration.command.ai", defaultValue = "3")
	public static int	COMMAND_AI;

	@Property(key = "administration.command.addtitle", defaultValue = "3")
	public static int	COMMAND_ADDTITLE;

	@Property(key = "administration.command.addset", defaultValue = "3")
	public static int	COMMAND_ADDSET;

	@Property(key = "administration.command.adddrop", defaultValue = "3")
	public static int	COMMAND_ADDDROP;

	@Property(key = "administration.command.advsendfakeserverpacket", defaultValue = "3")
	public static int	COMMAND_ADVSENDFAKESERVERPACKET;

	@Property(key = "administration.command.announce", defaultValue = "3")
	public static int	COMMAND_ANNOUNCE;

	@Property(key = "administration.command.announce_faction", defaultValue = "3")
	public static int	COMMAND_ANNOUNCE_FACTION;

	@Property(key = "administration.command.announcements", defaultValue = "3")
	public static int	COMMAND_ANNOUNCEMENTS;

	@Property(key = "administration.command.appearance", defaultValue = "3")
	public static int	COMMAND_APPEARANCE;

	@Property(key = "administration.command.ban", defaultValue = "3")
	public static int	COMMAND_BAN;

	@Property(key = "administration.command.bk", defaultValue = "3")
	public static int	COMMAND_BK;

	@Property(key = "administration.command.configure", defaultValue = "3")
	public static int	COMMAND_CONFIGURE;

	@Property(key = "administration.command.deletespawn", defaultValue = "3")
	public static int	COMMAND_DELETESPAWN;

	@Property(key = "administration.command.dye", defaultValue = "3")
	public static int	COMMAND_DYE;

	@Property(key = "administration.command.gag", defaultValue = "3")
	public static int	COMMAND_GAG;

	@Property(key = "administration.command.goto", defaultValue = "3")
	public static int	COMMAND_GOTO;

	@Property(key = "administration.command.givemissingskills", defaultValue = "3")
	public static int	COMMAND_GIVEMISSINGSKILLS;

	@Property(key = "administration.command.heal", defaultValue = "3")
	public static int	COMMAND_HEAL;

	@Property(key = "administration.command.info", defaultValue = "3")
	public static int	COMMAND_INFO;

	@Property(key = "administration.command.invis", defaultValue = "3")
	public static int	COMMAND_INVIS;

	@Property(key = "administration.command.invul", defaultValue = "3")
	public static int	COMMAND_INVUL;

	@Property(key = "administration.command.kick", defaultValue = "3")
	public static int	COMMAND_KICK;

	@Property(key = "administration.command.kill", defaultValue = "3")
	public static int	COMMAND_KILL;

	@Property(key = "administration.command.kinah", defaultValue = "3")
	public static int	COMMAND_KINAH;

	@Property(key = "administration.command.legion", defaultValue = "3")
	public static int	COMMAND_LEGION;

	@Property(key = "administration.command.morph", defaultValue = "3")
	public static int	COMMAND_MORPH;

	@Property(key = "administration.command.moveplayertoplayer", defaultValue = "3")
	public static int	COMMAND_MOVEPLAYERTOPLAYER;

	@Property(key = "administration.command.moveto", defaultValue = "3")
	public static int	COMMAND_MOVETO;

	@Property(key = "administration.command.movetonpc", defaultValue = "3")
	public static int	COMMAND_MOVETONPC;

	@Property(key = "administration.command.movetoplayer", defaultValue = "3")
	public static int	COMMAND_MOVETOPLAYER;

	@Property(key = "administration.command.movetome", defaultValue = "3")
	public static int	COMMAND_MOVETOME;

	@Property(key = "administration.command.notice", defaultValue = "3")
	public static int	COMMAND_NOTICE;

	@Property(key = "administration.command.petition", defaultValue = "3")
	public static int	COMMAND_PETITION;

	@Property(key = "administration.command.playerinfo", defaultValue = "3")
	public static int	COMMAND_PLAYERINFO;

	@Property(key = "administration.command.prison", defaultValue = "3")
	public static int	COMMAND_PRISON;

	@Property(key = "administration.command.promote", defaultValue = "3")
	public static int	COMMAND_PROMOTE;

	@Property(key = "administration.command.questcommand", defaultValue = "3")
	public static int	COMMAND_QUESTCOMMAND;

	@Property(key = "administration.command.reload", defaultValue = "3")
	public static int	COMMAND_RELOAD;

	@Property(key = "administration.command.reloadspawns", defaultValue = "3")
	public static int	COMMAND_RELOADSPAWNS;

	@Property(key = "administration.command.remove", defaultValue = "3")
	public static int	COMMAND_REMOVE;

	@Property(key = "administration.command.resurrect", defaultValue = "3")
	public static int	COMMAND_RESURRECT;

	@Property(key = "administration.command.revoke", defaultValue = "3")
	public static int	COMMAND_REVOKE;

	@Property(key = "administration.command.savespawndata", defaultValue = "3")
	public static int	COMMAND_SAVESPAWNDATA;

	@Property(key = "administration.command.sendfakeserverpacket", defaultValue = "3")
	public static int	COMMAND_SENDFAKESERVERPACKET;

	@Property(key = "administration.command.sendrawpacket", defaultValue = "3")
	public static int	COMMAND_SENDRAWPACKET;

	@Property(key = "administration.command.setap", defaultValue = "3")
	public static int	COMMAND_SETAP;

	@Property(key = "administration.command.setclass", defaultValue = "3")
	public static int	COMMAND_SETCLASS;

	@Property(key = "administration.command.setexp", defaultValue = "3")
	public static int	COMMAND_SETEXP;

	@Property(key = "administration.command.setlevel", defaultValue = "3")
	public static int	COMMAND_SETLEVEL;

	@Property(key = "administration.command.settitle", defaultValue = "3")
	public static int	COMMAND_SETTITLE;

	@Property(key = "administration.command.siege", defaultValue = "3")
	public static int	COMMAND_SIEGE;

	@Property(key = "administration.command.spawnnpc", defaultValue = "3")
	public static int	COMMAND_SPAWNNPC;

	@Property(key = "administration.command.unloadspawn", defaultValue = "3")
	public static int	COMMAND_UNLOADSPAWN;

	@Property(key = "administration.command.addskill", defaultValue = "3")
	public static int	COMMAND_ADDSKILL;

	@Property(key = "administration.command.speed", defaultValue = "3")
	public static int	COMMAND_SPEED;

	@Property(key = "administration.command.system", defaultValue = "3")
	public static int	COMMAND_SYSTEM;

	@Property(key = "administration.command.unstuck", defaultValue = "3")
	public static int	COMMAND_UNSTUCK;

	@Property(key = "administration.command.weather", defaultValue = "3")
	public static int	COMMAND_WEATHER;

	@Property(key = "administration.command.zone", defaultValue = "3")
	public static int	COMMAND_ZONE;

	@Property(key = "administration.command.html", defaultValue = "3")
	public static int	COMMAND_HTML;
}