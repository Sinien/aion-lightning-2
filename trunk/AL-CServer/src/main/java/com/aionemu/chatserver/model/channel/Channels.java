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
package com.aionemu.chatserver.model.channel;

import java.nio.charset.Charset;
import java.util.Arrays;

import com.aionemu.chatserver.model.PlayerClass;
import com.aionemu.chatserver.model.Race;
import com.aionemu.chatserver.model.WorldMapType;
import com.aionemu.chatserver.service.GameServerService;

/**
 * @author ATracer
 */
public enum Channels
{
	/**
	 * LFG channels
	 */
	LFG_E(new LfgChannel(Race.ELYOS), "@\u0001partyFind_PF\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	LFG_A(new LfgChannel(Race.ASMODIANS), "@\u0001partyFind_PF\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	
	/**
	 * Trade channels
	 */
	TRADE_POETA_E(new TradeChannel(Race.ELYOS), "@\u0001trade_lf1\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	TRADE_VERTERON_E(new TradeChannel(Race.ELYOS), "@\u0001trade_LF1A\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	TRADE_SANCTUM_E(new TradeChannel(Race.ELYOS), "@\u0001trade_LC1\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	TRADE_ELTNEN_E(new TradeChannel(Race.ELYOS), "@\u0001trade_lf2\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	TRADE_THEOMOBOS_E(new TradeChannel(Race.ELYOS), "@\u0001trade_lf2a\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	TRADE_HEIRON_E(new TradeChannel(Race.ELYOS), "@\u0001trade_LF3\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	TRADE_ISHALGEN_E(new TradeChannel(Race.ELYOS), "@\u0001trade_df1\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	TRADE_MORHEIM_E(new TradeChannel(Race.ELYOS), "@\u0001trade_df2\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	TRADE_PANDAEMONIUM_E(new TradeChannel(Race.ELYOS), "@\u0001trade_DC1\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	TRADE_BELUSLAN_E(new TradeChannel(Race.ELYOS), "@\u0001trade_DF3\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	TRADE_ALTGARD_E(new TradeChannel(Race.ELYOS), "@\u0001trade_DF1A\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	TRADE_BRUSTHONIN_E(new TradeChannel(Race.ELYOS), "@\u0001trade_DF2A\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	TRADE_ABYSS_E(new TradeChannel(Race.ELYOS), "@\u0001trade_Ab1\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	
	
	TRADE_POETA_A(new TradeChannel(Race.ASMODIANS), "@\u0001trade_lf1\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	TRADE_VERTERON_A(new TradeChannel(Race.ASMODIANS), "@\u0001trade_LF1A\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	TRADE_SANCTUM_A(new TradeChannel(Race.ASMODIANS), "@\u0001trade_LC1\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	TRADE_ELTNEN_A(new TradeChannel(Race.ASMODIANS), "@\u0001trade_lf2\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	TRADE_THEOMOBOS_A(new TradeChannel(Race.ASMODIANS), "@\u0001trade_lf2a\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	TRADE_HEIRON_A(new TradeChannel(Race.ASMODIANS), "@\u0001trade_LF3\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	TRADE_ISHALGEN_A(new TradeChannel(Race.ASMODIANS), "@\u0001trade_df1\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	TRADE_MORHEIM_A(new TradeChannel(Race.ASMODIANS), "@\u0001trade_df2\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	TRADE_PANDAEMONIUM_A(new TradeChannel(Race.ASMODIANS), "@\u0001trade_DC1\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	TRADE_BELUSLAN_A(new TradeChannel(Race.ASMODIANS), "@\u0001trade_DF3\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	TRADE_ALTGARD_A(new TradeChannel(Race.ASMODIANS), "@\u0001trade_DF1A\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	TRADE_BRUSTHONIN_A(new TradeChannel(Race.ASMODIANS), "@\u0001trade_DF2A\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	TRADE_ABYSS_A(new TradeChannel(Race.ASMODIANS), "@\u0001trade_Ab1\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	
	/**
	 * Region channels
	 */
	REGION_POETA_E(new RegionChannel(WorldMapType.POETA.getId(), Race.ELYOS), "@\u0001public_lf1\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	REGION_VERTERON_E(new RegionChannel(WorldMapType.VERTERON.getId(), Race.ELYOS), "@\u0001public_LF1A\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	REGION_SANCTUM_E(new RegionChannel(WorldMapType.SANCTUM.getId(), Race.ELYOS), "@\u0001public_LC1\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	REGION_ELTNEN_E(new RegionChannel(WorldMapType.ELTNEN.getId(), Race.ELYOS), "@\u0001public_lf2\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	REGION_THEOMOBOS_E(new RegionChannel(WorldMapType.THEOMOBOS.getId(), Race.ELYOS), "@\u0001public_lf2a\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	REGION_HEIRON_E(new RegionChannel(WorldMapType.HEIRON.getId(), Race.ELYOS), "@\u0001public_LF3\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	REGION_ISHALGEN_E(new RegionChannel(WorldMapType.ISHALGEN.getId(), Race.ELYOS), "@\u0001public_df1\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	REGION_MORHEIM_E(new RegionChannel(WorldMapType.MORHEIM.getId(), Race.ELYOS), "@\u0001public_df2\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	REGION_PANDAEMONIUM_E(new RegionChannel(WorldMapType.PANDAEMONIUM.getId(), Race.ELYOS), "@\u0001public_DC1\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	REGION_BELUSLAN_E(new RegionChannel(WorldMapType.BELUSLAN.getId(), Race.ELYOS), "@\u0001public_DF3\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	REGION_ALTGARD_E(new RegionChannel(WorldMapType.ALTGARD.getId(), Race.ELYOS), "@\u0001public_DF1A\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	REGION_BRUSTHONIN_E(new RegionChannel(WorldMapType.BRUSTHONIN.getId(), Race.ELYOS), "@\u0001public_DF2A\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	REGION_ABYSS_E(new RegionChannel(WorldMapType.RESHANTA.getId(), Race.ELYOS), "@\u0001public_Ab1\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	
	REGION_POETA_A(new RegionChannel(WorldMapType.POETA.getId(), Race.ASMODIANS), "@\u0001public_lf1\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	REGION_VERTERON_A(new RegionChannel(WorldMapType.VERTERON.getId(), Race.ASMODIANS), "@\u0001public_LF1A\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	REGION_SANCTUM_A(new RegionChannel(WorldMapType.SANCTUM.getId(), Race.ASMODIANS), "@\u0001public_LC1\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	REGION_ELTNEN_A(new RegionChannel(WorldMapType.ELTNEN.getId(), Race.ASMODIANS), "@\u0001public_lf2\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	REGION_THEOMOBOS_A(new RegionChannel(WorldMapType.THEOMOBOS.getId(), Race.ASMODIANS), "@\u0001public_lf2a\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	REGION_HEIRON_A(new RegionChannel(WorldMapType.HEIRON.getId(), Race.ASMODIANS), "@\u0001public_LF3\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	REGION_ISHALGEN_A(new RegionChannel(WorldMapType.ISHALGEN.getId(), Race.ASMODIANS), "@\u0001public_df1\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	REGION_MORHEIM_A(new RegionChannel(WorldMapType.MORHEIM.getId(), Race.ASMODIANS), "@\u0001public_df2\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	REGION_PANDAEMONIUM_A(new RegionChannel(WorldMapType.PANDAEMONIUM.getId(), Race.ASMODIANS), "@\u0001public_DC1\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	REGION_BELUSLAN_A(new RegionChannel(WorldMapType.BELUSLAN.getId(), Race.ASMODIANS), "@\u0001public_DF3\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	REGION_ALTGARD_A(new RegionChannel(WorldMapType.ALTGARD.getId(), Race.ASMODIANS), "@\u0001public_DF1A\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	REGION_BRUSTHONIN_A(new RegionChannel(WorldMapType.BRUSTHONIN.getId(), Race.ASMODIANS), "@\u0001public_DF2A\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	REGION_ABYSS_A(new RegionChannel(WorldMapType.RESHANTA.getId(), Race.ASMODIANS), "@\u0001public_Ab1\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	
	/**
	 * Job channels
	 */
	JOB_GLADIATOR_E(new JobChannel(PlayerClass.GLADIATOR, Race.ELYOS), "@\u0001job_Gladiator\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	JOB_GLADIATOR_A(new JobChannel(PlayerClass.GLADIATOR, Race.ASMODIANS), "@\u0001job_Gladiator\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	
	JOB_TEMPLAR_E(new JobChannel(PlayerClass.TEMPLAR, Race.ELYOS), "@\u0001job_Templar\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	JOB_TEMPLAR_A(new JobChannel(PlayerClass.TEMPLAR, Race.ASMODIANS), "@\u0001job_Templar\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	
	JOB_SORCERER_E(new JobChannel(PlayerClass.SORCERER, Race.ELYOS), "@\u0001job_Sorcerer\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	JOB_SORCERER_A(new JobChannel(PlayerClass.SORCERER, Race.ASMODIANS), "@\u0001job_Sorcerer\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	
	JOB_SPIRITMASTER_E(new JobChannel(PlayerClass.SPIRIT_MASTER, Race.ELYOS), "@\u0001job_Spiritmaster\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	JOB_SPIRITMASTER_A(new JobChannel(PlayerClass.SPIRIT_MASTER, Race.ASMODIANS), "@\u0001job_Spiritmaster\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	
	JOB_CHANTER_E(new JobChannel(PlayerClass.CHANTER, Race.ELYOS), "@\u0001job_Chanter\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	JOB_CHANTER_A(new JobChannel(PlayerClass.CHANTER, Race.ASMODIANS), "@\u0001job_Chanter\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	
	JOB_RANGER_E(new JobChannel(PlayerClass.RANGER, Race.ELYOS), "@\u0001job_Ranger\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	JOB_RANGER_A(new JobChannel(PlayerClass.RANGER, Race.ASMODIANS), "@\u0001job_Ranger\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	
	JOB_ASSASSIN_E(new JobChannel(PlayerClass.ASSASSIN, Race.ELYOS), "@\u0001job_Assassin\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	JOB_ASSASSIN_A(new JobChannel(PlayerClass.ASSASSIN, Race.ASMODIANS), "@\u0001job_Assassin\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR"),
	
	JOB_CLERIC_E(new JobChannel(PlayerClass.CLERIC, Race.ELYOS), "@\u0001job_Cleric\u0001" + GameServerService.GAMESERVER_ID + ".0.AION.KOR"),
	JOB_CLERIC_A(new JobChannel(PlayerClass.CLERIC, Race.ASMODIANS), "@\u0001job_Cleric\u0001" + GameServerService.GAMESERVER_ID + ".1.AION.KOR");
	
	private Channel channel;
	private byte[] identifier;
	
	/**
	 * 
	 * @param channel
	 * @param identifier
	 */
	private Channels(Channel channel, String identifier)
	{
		this.channel = channel;
		this.identifier = identifier.getBytes(Charset.forName("UTF-16le"));
	}

	/**
	 * @return the channel
	 */
	public Channel getChannel()
	{
		return channel;
	}

	/**
	 * @return the identifier
	 */
	public byte[] getIdentifier()
	{
		return identifier;
	}

	/**
	 * 
	 * @param channelId
	 * @return
	 */
	public static Channel getChannelById(int channelId)
	{
		for(Channels channel : values())
		{
			if(channel.getChannel().getChannelId() == channelId)
				return channel.getChannel();
		}
		throw new IllegalArgumentException("Wrong channel id provided");
	}
	
	/**
	 * 
	 * @param identifier
	 * @return
	 */
	public static Channel getChannelByIdentifier(byte[] identifier)
	{
		for(Channels channel : values())
		{
			if(Arrays.equals(channel.getIdentifier(), identifier))
				return channel.getChannel();
		}
		return null;
	}

}
