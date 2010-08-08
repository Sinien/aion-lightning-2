/*
 * This file is part of aion-emu <aion-emu.com>.
 *
 *  aion-emu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-emu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-emu.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.network.aion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.aionemu.gameserver.network.aion.serverpackets.*;

/**
 * This class is holding opcodes for all server packets. It's used only to have all opcodes in one place
 * 
 * @author Luno
 * @author alexa026
 * @author ATracer
 * @author avol
 * @author orz
 */
public class ServerPacketsOpcodes
{
	private static Map<Class<? extends AionServerPacket>, Integer>	opcodes	= new HashMap<Class<? extends AionServerPacket>, Integer>();

	static
	{
		Set<Integer> idSet = new HashSet<Integer>();

		addPacketOpcode(SM_STATUPDATE_MP.class, 0x00, idSet);// 1.9
		addPacketOpcode(SM_STATUPDATE_HP.class, 0x01, idSet);// 1.9
		addPacketOpcode(SM_CHAT_INIT.class, 0x02, idSet); // 1.9
		addPacketOpcode(SM_CHANNEL_INFO.class, 0x03, idSet);// 1.9
		addPacketOpcode(SM_MACRO_RESULT.class, 0x04, idSet); // 1.9
		addPacketOpcode(SM_MACRO_LIST.class, 0x05, idSet);// 1.9
		addPacketOpcode(SM_NICKNAME_CHECK_RESPONSE.class, 0x07, idSet);// 1.9
		addPacketOpcode(SM_RIFT_ANNOUNCE.class, 0x08, idSet);// 1.9
		addPacketOpcode(SM_SET_BIND_POINT.class, 0x09, idSet);// 1.9
		addPacketOpcode(SM_ABYSS_RANK.class, 0x0B, idSet);// 1.9
		addPacketOpcode(SM_FRIEND_UPDATE.class, 0x0C, idSet);
		addPacketOpcode(SM_PETITION.class, 0x0D, idSet);// 1.9
		addPacketOpcode(SM_RECIPE_DELETE.class, 0x0E, idSet); // 1.9
		addPacketOpcode(SM_LEARN_RECIPE.class, 0x0F, idSet);// 1.9
		addPacketOpcode(SM_FLY_TIME.class, 0x10, idSet);// 1.9
		addPacketOpcode(SM_DELETE.class, 0x12, idSet);// 1.9
		addPacketOpcode(SM_PLAYER_MOVE.class, 0x13, idSet);// 1.9
		addPacketOpcode(SM_MESSAGE.class, 0x14, idSet);// 1.9
		addPacketOpcode(SM_LOGIN_QUEUE.class, 0x15, idSet); // 1.9
		addPacketOpcode(SM_INVENTORY_INFO.class, 0x16, idSet);// 1.9
		addPacketOpcode(SM_SYSTEM_MESSAGE.class, 0x17, idSet);// 1.9
		addPacketOpcode(SM_DELETE_ITEM.class, 0x18, idSet);
		addPacketOpcode(SM_INVENTORY_UPDATE.class, 0x19, idSet);// 1.9
		addPacketOpcode(SM_UI_SETTINGS.class, 0x1A, idSet);// 1.9
		addPacketOpcode(SM_UPDATE_ITEM.class, 0x1B, idSet);// 1.9
		addPacketOpcode(SM_PLAYER_INFO.class, 0x1C, idSet);// 1.9
		addPacketOpcode(SM_GATHER_STATUS.class, 0x1E, idSet);// 1.9
		addPacketOpcode(SM_CASTSPELL.class, 0x1F, idSet);// 1.9
		addPacketOpcode(SM_UPDATE_PLAYER_APPEARANCE.class, 0x20, idSet);// 1.9
		addPacketOpcode(SM_GATHER_UPDATE.class, 0x21, idSet);// 1.9
		addPacketOpcode(SM_STATUPDATE_DP.class, 0x22, idSet);// 1.9
		addPacketOpcode(SM_ATTACK_STATUS.class, 0x23, idSet);// 1.9
		addPacketOpcode(SM_STATUPDATE_EXP.class, 0x24, idSet);// 1.9
		addPacketOpcode(SM_DP_INFO.class, 0x25, idSet);// 1.9
		addPacketOpcode(SM_LEGION_TABS.class, 0x28, idSet);// 1.9
		addPacketOpcode(SM_LEGION_UPDATE_NICKNAME.class, 0x29, idSet);// 1.9
		addPacketOpcode(SM_NPC_INFO.class, 0x2A, idSet);// 1.9
		addPacketOpcode(SM_ENTER_WORLD_CHECK.class, 0x2B, idSet);// 1.9
		addPacketOpcode(SM_PLAYER_SPAWN.class, 0x2D, idSet);// 1.9
		addPacketOpcode(SM_GATHERABLE_INFO.class, 0x2F, idSet);// 1.9
		addPacketOpcode(SM_TELEPORT_LOC.class, 0x30, idSet);// 1.9
		addPacketOpcode(SM_ATTACK.class, 0x32, idSet);// 1.9
		addPacketOpcode(SM_MOVE.class, 0x35, idSet);// 1.9
		addPacketOpcode(SM_TRANSFORM.class, 0x36, idSet);// 1.9
		addPacketOpcode(SM_DIALOG_WINDOW.class, 0x38, idSet);// 1.9
		addPacketOpcode(SM_SELL_ITEM.class, 0x3A, idSet);// 1.9
		addPacketOpcode(SM_VIEW_PLAYER_DETAILS.class, 0x3F, idSet);
		addPacketOpcode(SM_PLAYER_STATE.class, 0x40, idSet);// 1.9
		addPacketOpcode(SM_WEATHER.class, 0x41, idSet);// 1.9
		addPacketOpcode(SM_GAME_TIME.class, 0x42, idSet);// 1.9
		addPacketOpcode(SM_EMOTION.class, 0x43, idSet);// 1.9
		addPacketOpcode(SM_LOOKATOBJECT.class, 0x44, idSet);// 1.9
		addPacketOpcode(SM_TIME_CHECK.class, 0x45, idSet);// 1.9
		addPacketOpcode(SM_SKILL_CANCEL.class, 0x46, idSet);// 1.9
		addPacketOpcode(SM_TARGET_SELECTED.class, 0x47, idSet);// 1.9
		addPacketOpcode(SM_SKILL_LIST.class, 0x48, idSet);// 1.9
		addPacketOpcode(SM_CASTSPELL_END.class, 0x49, idSet);// 1.9
		addPacketOpcode(SM_SKILL_ACTIVATION.class, 0x4A, idSet);// 1.9
		addPacketOpcode(SM_STIGMA_SKILL_REMOVE.class, 0x4B, idSet);// 1.9
		addPacketOpcode(SM_ABNORMAL_EFFECT.class, 0x4E, idSet);// 1.9
		addPacketOpcode(SM_ABNORMAL_STATE.class, 0x4F, idSet);// 1.9
		addPacketOpcode(SM_QUESTION_WINDOW.class, 0x50, idSet);// 1.9
		addPacketOpcode(SM_SKILL_COOLDOWN.class, 0x51, idSet);// 1.9
		addPacketOpcode(SM_INFLUENCE_RATIO.class, 0x53, idSet);// 1.9
		addPacketOpcode(SM_NAME_CHANGE.class, 0x54, idSet);// 1.9
		addPacketOpcode(SM_GROUP_INFO.class, 0x56, idSet);// 1.9
		addPacketOpcode(SM_SHOW_NPC_ON_MAP.class, 0x57, idSet);// 1.9
		addPacketOpcode(SM_GROUP_MEMBER_INFO.class, 0x59, idSet);// 1.9
		addPacketOpcode(SM_QUIT_RESPONSE.class, 0x5E, idSet);// 1.9
		addPacketOpcode(SM_LEVEL_UPDATE.class, 0x62, idSet);// 1.9
		addPacketOpcode(SM_KEY.class, 0x64, idSet); // 1.9
		addPacketOpcode(SM_EXCHANGE_REQUEST.class, 0x66, idSet);// 1.9
		addPacketOpcode(SM_SUMMON_PANEL_REMOVE.class, 0x67, idSet);// testing
		addPacketOpcode(SM_EXCHANGE_ADD_ITEM.class, 0x69, idSet);// 1.9
		addPacketOpcode(SM_EXCHANGE_CONFIRMATION.class, 0x6A, idSet);// 1.9
		addPacketOpcode(SM_EXCHANGE_ADD_KINAH.class, 0x6B, idSet);// 1.9
		addPacketOpcode(SM_EMOTION_LIST.class, 0x6D, idSet);// 1.9
		addPacketOpcode(SM_TARGET_UPDATE.class, 0x6F, idSet);// 1.9
		addPacketOpcode(SM_LEGION_UPDATE_SELF_INTRO.class, 0x75, idSet);// 1.9
		addPacketOpcode(SM_RIFT_STATUS.class, 0x76, idSet); // 1.9
		addPacketOpcode(SM_QUEST_ACCEPTED.class, 0x78, idSet);// 1.9
		addPacketOpcode(SM_QUEST_LIST.class, 0x79, idSet); // 1.9
		addPacketOpcode(SM_PING_RESPONSE.class, 0x7C, idSet);// 1.9
		addPacketOpcode(SM_NEARBY_QUESTS.class, 0x7D, idSet); // 1.9
		addPacketOpcode(SM_CUBE_UPDATE.class, 0x7E, idSet); // 1.9
		addPacketOpcode(SM_FRIEND_LIST.class, 0x80, idSet);// 1.9
		addPacketOpcode(SM_UPDATE_NOTE.class, 0x84, idSet); // 1.9
		addPacketOpcode(SM_ITEM_COOLDOWN.class, 0x85, idSet);// 1.9
		addPacketOpcode(SM_PLAY_MOVIE.class, 0x87, idSet); // 1.9
		addPacketOpcode(SM_LEGION_INFO.class, 0x8A, idSet);// 1.9
		addPacketOpcode(SM_LEGION_LEAVE_MEMBER.class, 0x8C, idSet);// 1.9
		addPacketOpcode(SM_LEGION_ADD_MEMBER.class, 0x8D, idSet);// 1.9
		addPacketOpcode(SM_LEGION_UPDATE_TITLE.class, 0x8E, idSet);// 1.9
		addPacketOpcode(SM_LEGION_UPDATE_MEMBER.class, 0x8F, idSet);// 1.9
		addPacketOpcode(SM_BROKER_REGISTRATION_SERVICE.class, 0x93, idSet);// 1.9
		addPacketOpcode(SM_BROKER_SETTLED_LIST.class, 0x95, idSet);// 1.9
		addPacketOpcode(SM_SUMMON_OWNER_REMOVE.class, 0x96, idSet);// testing
		addPacketOpcode(SM_SUMMON_PANEL.class, 0x97, idSet);// testing
		addPacketOpcode(SM_SUMMON_UPDATE.class, 0x99, idSet);// testing
		addPacketOpcode(SM_LEGION_EDIT.class, 0x9A, idSet);// 1.9
		addPacketOpcode(SM_LEGION_MEMBERLIST.class, 0x9B, idSet);// 1.9
		addPacketOpcode(SM_SUMMON_USESKILL.class, 0x9E, idSet);// testing
		addPacketOpcode(SM_MAIL_SERVICE.class, 0x9F, idSet);// 1.9
		addPacketOpcode(SM_PRIVATE_STORE.class, 0xA2, idSet);
		addPacketOpcode(SM_ABYSS_RANK_UPDATE.class, 0xA4, idSet);// testing
		addPacketOpcode(SM_GROUP_LOOT.class, 0xA5, idSet);
		addPacketOpcode(SM_MAY_LOGIN_INTO_GAME.class, 0xA7, idSet);// 1.9
		addPacketOpcode(SM_PONG.class, 0xAA, idSet);// 1.9
		addPacketOpcode(SM_PLAYER_ID.class, 0xAB, idSet);// 1.9
		addPacketOpcode(SM_KISK_UPDATE.class, 0xAC, idSet);// 1.9
		addPacketOpcode(SM_BROKER_ITEMS.class, 0xAE, idSet);// 1.9
		addPacketOpcode(SM_PRIVATE_STORE_NAME.class, 0xAF, idSet);// 1.9
		addPacketOpcode(SM_BROKER_REGISTERED_LIST.class, 0xB1, idSet);// 1.9
		addPacketOpcode(SM_ASCENSION_MORPH.class, 0xB2, idSet);// 1.9
		addPacketOpcode(SM_CRAFT_UPDATE.class, 0xB3, idSet);// was CD
		addPacketOpcode(SM_CUSTOM_SETTINGS.class, 0xB4, idSet);// 1.9
		addPacketOpcode(SM_ITEM_USAGE_ANIMATION.class, 0xB5, idSet);// 1.9
		addPacketOpcode(SM_DUEL.class, 0xB7, idSet);// 1.9
		addPacketOpcode(SM_RESURRECT.class, 0xBE, idSet);// 1.9
		addPacketOpcode(SM_DIE.class, 0xBF, idSet);// 1.9
		addPacketOpcode(SM_TELEPORT_MAP.class, 0xC0, idSet);// 1.9
		addPacketOpcode(SM_FORCED_MOVE.class, 0xC1, idSet);// 1.9		
		addPacketOpcode(SM_WAREHOUSE_INFO.class, 0xC4, idSet);// 1.9
		addPacketOpcode(SM_DELETE_WAREHOUSE_ITEM.class, 0xC6, idSet);// 1.9
		addPacketOpcode(SM_WAREHOUSE_UPDATE.class, 0xC7, idSet);// 1.9
		addPacketOpcode(SM_UPDATE_WAREHOUSE_ITEM.class, 0xC9, idSet);// 1.9		
		addPacketOpcode(SM_TITLE_LIST.class, 0xCC, idSet);// 1.9
		addPacketOpcode(SM_TITLE_SET.class, 0xCF, idSet);// 1.9
		addPacketOpcode(SM_CRAFT_ANIMATION.class, 0xD0, idSet);
		addPacketOpcode(SM_TITLE_UPDATE.class, 0xD1, idSet);// 1.9
		addPacketOpcode(SM_LEGION_SEND_EMBLEM.class, 0xD3, idSet);// 1.9
		addPacketOpcode(SM_LEGION_UPDATE_EMBLEM.class, 0xD5, idSet);// testing
		addPacketOpcode(SM_FRIEND_RESPONSE.class, 0xDA, idSet);// 1.9
		addPacketOpcode(SM_BLOCK_LIST.class, 0xDC, idSet);// 1.9
		addPacketOpcode(SM_BLOCK_RESPONSE.class, 0xDD, idSet);// 1.9
		addPacketOpcode(SM_FRIEND_NOTIFY.class, 0xDF, idSet);// 1.9
		addPacketOpcode(SM_USE_OBJECT.class, 0xE3, idSet);// 1.9
		addPacketOpcode(SM_CHARACTER_LIST.class, 0xE4, idSet);// 1.9
		addPacketOpcode(SM_L2AUTH_LOGIN_CHECK.class, 0xE5, idSet);// 1.9
		addPacketOpcode(SM_DELETE_CHARACTER.class, 0xE6, idSet);
		addPacketOpcode(SM_CREATE_CHARACTER.class, 0xE7, idSet);// 1.9
		addPacketOpcode(SM_TARGET_IMMOBILIZE.class, 0xE8, idSet);
		addPacketOpcode(SM_RESTORE_CHARACTER.class, 0xE9, idSet);// 1.9
		addPacketOpcode(SM_LOOT_ITEMLIST.class, 0xEA, idSet);// 1.9
		addPacketOpcode(SM_LOOT_STATUS.class, 0xEB, idSet);// 1.9
		addPacketOpcode(SM_MANTRA_EFFECT.class, 0xEC, idSet);// 1.9		
		addPacketOpcode(SM_RECIPE_LIST.class, 0xED, idSet);// testing
		addPacketOpcode(SM_SIEGE_LOCATION_INFO.class, 0xEF, idSet);// 1.9
		addPacketOpcode(SM_PLAYER_SEARCH.class, 0xF1, idSet);// 1.9
		addPacketOpcode(SM_ALLIANCE_MEMBER_INFO.class, 0xF2, idSet);// 1.9
		addPacketOpcode(SM_ALLIANCE_INFO.class, 0xF3, idSet);// 1.9
		addPacketOpcode(SM_LEAVE_GROUP_MEMBER.class, 0xF5, idSet);// 1.9
		addPacketOpcode(SM_ALLIANCE_READY_CHECK.class, 0xF6, idSet);// 1.9
		addPacketOpcode(SM_SHOW_BRAND.class, 0xF7, idSet);// 1.9
		addPacketOpcode(SM_PRICES.class, 0xF8, idSet);// 1.9
		addPacketOpcode(SM_TRADELIST.class, 0xFB, idSet);// 1.9
		addPacketOpcode(SM_VERSION_CHECK.class, 0xFC, idSet);// 1.9
		addPacketOpcode(SM_RECONNECT_KEY.class, 0xFD, idSet);// 1.9
		addPacketOpcode(SM_STATS_INFO.class, 0xFF, idSet);// 1.9
		addPacketOpcode(SM_CUSTOM_PACKET.class, 99999, idSet); // fake packet

		// Unrecognized Opcodes from 1.5.4:
		// addPacketOpcode(SM_BUY_LIST.class, 0x7E, idSet);

		// Unrecognized Opcodes from 1.5.0:
		// addPacketOpcode(SM_VIRTUAL_AUTH.class, 0xE4, idSet);
		// addPacketOpcode(SM_WAITING_LIST.class, 0x18, idSet);
	}

	static int getOpcode(Class<? extends AionServerPacket> packetClass)
	{
		Integer opcode = opcodes.get(packetClass);
		if(opcode == null)
			throw new IllegalArgumentException("There is no opcode for " + packetClass + " defined.");

		return opcode;
	}

	private static void addPacketOpcode(Class<? extends AionServerPacket> packetClass, int opcode, Set<Integer> idSet)
	{
		if(opcode < 0)
			return;

		if(idSet.contains(opcode))
			throw new IllegalArgumentException(String.format("There already exists another packet with id 0x%02X",
				opcode));

		idSet.add(opcode);
		opcodes.put(packetClass, opcode);
	}
}
