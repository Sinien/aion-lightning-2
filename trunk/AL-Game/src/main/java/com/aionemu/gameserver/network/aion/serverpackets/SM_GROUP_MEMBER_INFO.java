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
package com.aionemu.gameserver.network.aion.serverpackets;

import java.nio.ByteBuffer;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.gameobjects.stats.PlayerLifeStats;
import com.aionemu.gameserver.model.group.GroupEvent;
import com.aionemu.gameserver.model.group.PlayerGroup;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Lyahim, ATracer
 *
 */
public class SM_GROUP_MEMBER_INFO extends AionServerPacket
{
	private PlayerGroup group;
	private Player player;
	private GroupEvent event;
	
	public SM_GROUP_MEMBER_INFO(PlayerGroup group, Player player, GroupEvent event)
	{
		this.group = group;
		this.player = player;
		this.event = event;
	}
	
	@Override
	protected void writeImpl(AionConnection con, ByteBuffer buf)
	{		
		PlayerLifeStats pls = player.getLifeStats();
		PlayerCommonData pcd = player.getCommonData();
		WorldPosition wp = pcd.getPosition();
		
		writeD(buf, group.getGroupId());
		writeD(buf, player.getObjectId());
		writeD(buf, pls.getMaxHp());
		writeD(buf, pls.getCurrentHp());
		writeD(buf, pls.getMaxMp());
		writeD(buf, pls.getCurrentMp());
		writeD(buf, pls.getMaxFp()); //maxflighttime
		writeD(buf, pls.getCurrentFp()); //currentflighttime
		writeD(buf, wp.getMapId());
		writeD(buf, wp.getMapId());
		writeF(buf, wp.getX());
		writeF(buf, wp.getY());
		writeF(buf, wp.getZ());
		writeC(buf, pcd.getPlayerClass().getClassId()); //class id
		writeC(buf, pcd.getGender().getGenderId()); //gender id
		writeC(buf, pcd.getLevel()); //level
		writeC(buf, this.event.getId()); //something events
		writeH(buf, 0x01); //channel
		if (this.event == GroupEvent.MOVEMENT)
		{
			return;
		}
		writeS(buf, pcd.getName()); //name
		writeH(buf, 0x00); //unk
		writeH(buf, 0x00); //unk
		
		List<Effect> abnormalEffects = player.getEffectController().getAbnormalEffects();
		writeH(buf, abnormalEffects.size()); //Abnormal effects
		for(Effect effect : abnormalEffects)
		{
			writeD(buf, effect.getEffectorId()); //casterid
			writeH(buf, effect.getSkillId()); //spellid
			writeC(buf, effect.getSkillLevel()); //spell level
			writeC(buf, effect.getTargetSlot()); //unk ?
			writeD(buf, effect.getElapsedTime()); //estimatedtime
		}
		writeD(buf, 0x25F7); //unk 9719
	}
}
