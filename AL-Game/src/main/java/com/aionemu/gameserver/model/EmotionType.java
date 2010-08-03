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
package com.aionemu.gameserver.model;

/**
 * @author lyahim
 *
 */
public enum EmotionType
{
	UNK(-1),
	SELECT_TARGET(0),
	JUMP(1),
	SIT(2),
	STAND(3),
	CHAIR_SIT(4),
	CHAIR_UP(5),
	START_FLYTELEPORT(6),
	LAND_FLYTELEPORT(7),
	FLY(11),
	LAND(12),
	DIE(16),
	RESURRECT(17),
	EMOTE(19),
	END_DUEL(20),
	ATTACKMODE(22),
	NEUTRALMODE(23),
	WALK(24),
	RUN(25),
	SWITCH_DOOR(29),
	START_EMOTE(30),
	OPEN_PRIVATESHOP(31),
	CLOSE_PRIVATESHOP(32),
	START_EMOTE2(33), //why have 2 code?
	POWERSHARD_ON(34),
	POWERSHARD_OFF(35),
	ATTACKMODE2(36), //why have 2 code?
	NEUTRALMODE2(37), //why have 2 code?
	START_LOOT(38),
	END_LOOT(39),
	START_QUESTLOOT(40),
	END_QUESTLOOT(41);
	
	private int id;
	
	private EmotionType(int id)
	{
		this.id = id;
	}
	
	public int getTypeId()
	{
		return id;
	}
	
	
	public static EmotionType getEmotionTypeById(int id)
	{
		for(EmotionType emotionType : values())
		{
			if(emotionType.getTypeId() == id)
				return emotionType;
		}
		return UNK;
	}
	
}
