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
package com.aionemu.gameserver.network.aion.serverpackets;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.gameobjects.stats.StatEnum;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Emotion packet
 * 
 * @author SoulKeeper
 */
public class SM_EMOTION extends AionServerPacket
{
	/**
	 * Object id of emotion sender
	 */
	private int					senderObjectId;

	/**
	 * Some unknown variable
	 */
	private EmotionType					emotionType;

	/**
	 * ID of emotion
	 */
	private int					emotion;

	/**
	 * Object id of emotion target
	 */
	private int					targetObjectId;

	/**
	 * Temporary Speed..
	 */
	private float				speed = 6.0f;

	private int					state;

	private int 				baseAttackSpeed;
	private int 				currentAttackSpeed;

	/**
	 * Coordinates of player
	 */
	private float				x;
	private float				y;
	private float				z;
	private byte				heading;

	/**
	 * This constructor should be used when emotion and targetid is 0
	 * 
	 * @param creature
	 * @param emotionType
	 */
	public SM_EMOTION(Creature creature, EmotionType emotionType)
	{
		this(creature, emotionType, 0, 0);
	}

	/**
	 * Constructs new server packet with specified opcode
	 * 
	 * @param senderObjectId
	 *            who sended emotion
	 * @param unknown
	 *            Dunno what it is, can be 0x10 or 0x11
	 * @param emotionId
	 *            emotion to play
	 * @param emotionId
	 *            who target emotion
	 */
	public SM_EMOTION(Creature creature, EmotionType emotionType, int emotion, int targetObjectId)
	{
		this.senderObjectId = creature.getObjectId();
		this.emotionType = emotionType;
		this.emotion = emotion;
		this.targetObjectId = targetObjectId;
		this.state = creature.getState();
		this.baseAttackSpeed = creature.getGameStats().getBaseStat(StatEnum.ATTACK_SPEED);
		this.currentAttackSpeed = creature.getGameStats().getCurrentStat(StatEnum.ATTACK_SPEED);

		if (creature.isInState(CreatureState.FLYING))
			this.speed = creature.getGameStats().getCurrentStat(StatEnum.FLY_SPEED) / 1000f;
		else
			this.speed = creature.getGameStats().getCurrentStat(StatEnum.SPEED) / 1000f;
	}

	/**
	 * Used to open a door.
	 * 
	 * @param doorId
	 */
	public SM_EMOTION(int doorId)
	{
		this.senderObjectId = doorId;
		this.emotionType = EmotionType.SWITCH_DOOR;
	}
	
	/**
	 * New
	 *
	 */
	public SM_EMOTION(Player player, EmotionType emotionType, int emotion, float x, float y, float z, byte heading, int targetObjectId)
	{
		this.senderObjectId = player.getObjectId();
		this.emotionType = emotionType;
		this.emotion = emotion;
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;
		this.targetObjectId = targetObjectId;

		if (player.isInState(CreatureState.FLYING))
			this.speed = player.getGameStats().getCurrentStat(StatEnum.FLY_SPEED) / 1000f;
		else
			this.speed = player.getGameStats().getCurrentStat(StatEnum.SPEED) / 1000f;

		this.state = player.getState();
		this.baseAttackSpeed = player.getGameStats().getBaseStat(StatEnum.ATTACK_SPEED);
		this.currentAttackSpeed = player.getGameStats().getCurrentStat(StatEnum.ATTACK_SPEED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con, ByteBuffer buf)
	{
		writeD(buf, senderObjectId);
		writeC(buf, emotionType.getTypeId());
		switch(emotionType)
		{
			case SELECT_TARGET:
				// select target
				writeH(buf, state);
				writeF(buf, speed);
				break;
			case JUMP:
				// jump
				writeH(buf, state);
				writeF(buf, speed);
				break;
			case SIT:
				// sit
				writeH(buf, state);
				writeF(buf, speed);
				break;
			case STAND:
				// stand
				writeH(buf, state);
				writeF(buf, speed);
				break;
			case CHAIR_SIT:
				// sit (chair)
				writeH(buf, state);
				writeF(buf, speed);
				writeF(buf, x);
				writeF(buf, y);
				writeF(buf, z);
				writeC(buf, heading);
				break;
			case CHAIR_UP:
				// stand (chair)
				writeH(buf, state);
				writeF(buf, speed);
				writeF(buf, x);
				writeF(buf, y);
				writeF(buf, z);
				writeC(buf, heading);
				break;				
			case START_FLYTELEPORT:
				// fly teleport (start)
				writeH(buf, state);
				writeF(buf, speed);
				writeD(buf, emotion); // teleport Id
				break;
			case LAND_FLYTELEPORT:
				// fly teleport (land)
				writeH(buf, state);
				writeF(buf, speed);
				break;
			case FLY:
				// toggle flight mode
				writeH(buf, state);
				writeF(buf, speed);
				break;
			case LAND:
				// toggle land mode
				writeH(buf, state);
				writeF(buf, speed);
				break;
			case DIE:
				// die
				writeH(buf, state);
				writeF(buf, speed);
				writeD(buf, targetObjectId);
				break;
			case RESURRECT:
				// resurrect
				writeH(buf, state);
				writeF(buf, speed);
				break;						
			case EMOTE:
				// emote
				writeH(buf, state);
				writeF(buf, speed);
				writeD(buf, targetObjectId);
				writeH(buf, emotion);
				writeC(buf, 1);
				break;				
			case ATTACKMODE:
				// toggle attack mode
				writeH(buf, state);
				writeF(buf, speed);
				break;
			case NEUTRALMODE:
				// toggle normal mode
				writeH(buf, state);
				writeF(buf, speed);
				break;		
			case WALK:
				// toggle walk
				writeH(buf, state);
				writeF(buf, (speed - (speed * 75f) / 100f));
				break;
			case RUN:
				// toggle run
				writeH(buf, state);
				writeF(buf, speed);
				break;
			case SWITCH_DOOR:
				// toggle doors
				writeH(buf, 9);
				writeD(buf, 0);
				break;				
			case START_EMOTE:
				// emote startloop
				writeH(buf, state);
				writeF(buf, speed);
				writeH(buf, baseAttackSpeed);
				writeH(buf, currentAttackSpeed);
				break;								
			case OPEN_PRIVATESHOP:
				// private shop open
				writeH(buf, state);
				writeF(buf, speed);
				break;
			case CLOSE_PRIVATESHOP:
				// private shop close
				writeH(buf, state);
				writeF(buf, speed);
				break;
			case START_EMOTE2:
				// emote startloop
				writeH(buf, state);
				writeF(buf, speed);
				writeH(buf, baseAttackSpeed);
				writeH(buf, currentAttackSpeed);
				break;				
			case POWERSHARD_ON:
				// powershard on
				writeH(buf, state);
				writeF(buf, speed);
				break;
			case POWERSHARD_OFF:
				// powershard off
				writeH(buf, state);
				writeF(buf, speed);
				break;
			case ATTACKMODE2:
				// toggle attack mode
				writeH(buf, state);
				writeF(buf, speed);
				break;
			case NEUTRALMODE2:
				// toggle normal mode
				writeH(buf, state);
				writeF(buf, speed);
				break;
			case START_LOOT:
				// looting start
				writeH(buf, state);
				writeF(buf, speed);
				writeD(buf, targetObjectId);
                break;
			case END_LOOT:
				// looting end
				writeH(buf, state);
				writeF(buf, speed);
				writeD(buf, targetObjectId);
                break;
			case START_QUESTLOOT:
				// looting start (quest)
				writeH(buf, state);
				writeF(buf, speed);
				writeD(buf, targetObjectId);
				break;
			case END_QUESTLOOT:
				// looting end (quest)
				writeH(buf, state);
				writeF(buf, speed);
				writeD(buf, targetObjectId);
				break;
			default:
				writeH(buf, state);
				writeF(buf, speed);
				if(targetObjectId != 0)
				{
					writeD(buf, targetObjectId);
				}
		}
	}
}
