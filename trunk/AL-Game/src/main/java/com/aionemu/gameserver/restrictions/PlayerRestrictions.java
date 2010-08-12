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
package com.aionemu.gameserver.restrictions;

import com.aionemu.gameserver.model.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Monster;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.group.PlayerGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.effect.EffectId;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.SkillType;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author lord_rex
 * 
 */
public class PlayerRestrictions extends AbstractRestrictions
{	
	@Override
	public boolean canAffectBySkill(Player player, VisibleObject target)
	{
		Skill skill = player.getCastingSkill();
		if(skill == null)
			return false;

		if(((Creature) target).getLifeStats().isAlreadyDead() && !skill.getSkillTemplate().hasResurrectEffect())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.INVALID_TARGET());
			return false;
		}

		if(skill.getSkillTemplate().hasItemHealFpEffect() && !player.isInState(CreatureState.FLYING))
		{ // player must be flying when using flight potions
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_RESTRICTION_FLY_ONLY);
			return false;
		}

		if( (player.getEffectController().isAbnormalState(EffectId.CANT_ATTACK_STATE)) && (skill.getSkillTemplate().getSkillId() != 1968) )
			return false;

		if(player.isInState(CreatureState.PRIVATE_SHOP))
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_USE_ITEM_WHILE_PRIVATE_STORE);
			return false;
		}

		return true;
	}
	
	@Override
	public boolean canUseSkill(Player player, Skill skill)
	{
		// check if is casting to avoid multicast exploit
		// TODO cancel skill if other is used
		if(player.isCasting())
			return false;
		
		if ( (!player.canAttack()) && (skill.getSkillTemplate().getSkillId() != 1968) )
			return false;
		
		if(skill.getSkillTemplate().getType() == SkillType.MAGICAL 
			&& player.getEffectController().isAbnoramlSet(EffectId.SILENCE))
			return false;
		
		if(skill.getSkillTemplate().getType() == SkillType.PHYSICAL
			&& player.getEffectController().isAbnoramlSet(EffectId.BLOCKADE))
			return false;
		
		if(player.isSkillDisabled(skill.getSkillTemplate().getSkillId()))
			return false;

		return true;
	}

	@Override
	public boolean canInviteToGroup(Player player, Player target)
	{
		final PlayerGroup group = player.getPlayerGroup();
		
		if(group != null && group.isFull())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.FULL_GROUP());
			return false;
		}
		else if(group != null && player.getObjectId() != group.getGroupLeader().getObjectId())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.ONLY_GROUP_LEADER_CAN_INVITE());
			return false;
		}
		else if(target == null)
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.INVITED_PLAYER_OFFLINE());
			return false;
		}
		else if(target.getCommonData().getRace() != player.getCommonData().getRace())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_CANT_INVITE_OTHER_RACE());
			return false;
		}
		else if(target.sameObjectId(player.getObjectId()))
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.CANNOT_INVITE_YOURSELF());
			return false;
		}
		else if(target.getLifeStats().isAlreadyDead())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.SELECTED_TARGET_DEAD());
			return false;
		}
		else if(player.getLifeStats().isAlreadyDead())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.CANNOT_INVITE_BECAUSE_YOU_DEAD());
			return false;
		}
		
		if(target.isInGroup())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.PLAYER_IN_ANOTHER_GROUP(target.getName()));
			return false;
		}
		
		if (target.isInAlliance())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_ALREADY_OTHER_FORCE(target.getName()));
			return false;
		}
		
		return true;
	}

	@Override
	public boolean canInviteToAlliance(Player player, Player target)
	{
		if(target == null)
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_NO_USER_TO_INVITE());
			return false;
		}
		
		if(target.getCommonData().getRace() != player.getCommonData().getRace())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_CANT_INVITE_OTHER_RACE());
			return false;
		}
		
		final PlayerAlliance alliance = player.getPlayerAlliance();
		
		if (target.isInAlliance())
		{
			if (target.getPlayerAlliance() == alliance)
			{
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_HE_IS_ALREADY_MEMBER_OF_OUR_ALLIANCE(target.getName()));
				return false;
			}
			else
			{
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_ALREADY_OTHER_FORCE(target.getName()));
				return false;
			}
		}
		
		if(alliance != null && alliance.isFull())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_CANT_ADD_NEW_MEMBER());
			return false;
		}
		
		if(alliance != null && !alliance.hasAuthority(player.getObjectId()))
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_ONLY_PARTY_LEADER_CAN_LEAVE_ALLIANCE());
			return false;
		}
		
		if(target.sameObjectId(player.getObjectId()))
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_CAN_NOT_INVITE_SELF());
			return false;
		}
		
		if(target.getLifeStats().isAlreadyDead())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.SELECTED_TARGET_DEAD());
			return false;
		}
		
		if(player.getLifeStats().isAlreadyDead())
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_CANT_INVITE_WHEN_DEAD());
			return false;
		}
		
		if(target.isInGroup())
		{
			PlayerGroup targetGroup = target.getPlayerGroup();
			if (target != targetGroup.getGroupLeader())
			{
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_INVITE_PARTY_HIM(target.getName(), targetGroup.getGroupLeader().getName()));
				return false;
			}
			if (alliance != null && (targetGroup.size() + alliance.size() >= 24))
			{
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_INVITE_FAILED_NOT_ENOUGH_SLOT());
				return false;
			}
			
		}
		
		return true;
	}

	@Override
	public boolean canAttack(Player player, VisibleObject target)
	{
		if(target == null)
			return false;
		
		if(!(target instanceof Creature))
			return false;
		
		Creature creature = (Creature) target;
		
		if(creature.getLifeStats().isAlreadyDead())
			return false;
		
		if(creature instanceof Monster)
			return true;
		
		if(creature instanceof Npc)
		{
			Npc npc = (Npc) creature;
			if(!npc.isAggressiveTo(player))
				return false;
		}
		return true;
	}

	@Override
	public boolean canUseWarehouse(Player player)
	{
		if(player == null || !player.isOnline())
			return false;
		
		//TODO retail message to requestor and player
		if(player.isTrading())
			return false;
		
		return true;
	}

	@Override
	public boolean canTrade(Player player)
	{
		if(player == null || !player.isOnline())
			return false;
		
		//TODO retail message to requestor and player
		if(player.isTrading())
			return false;
		
		return true;
	}

	@Override
	public boolean canChat(Player player)
	{
		if(player == null || !player.isOnline())
			return false;
		
		return !player.isGagged();
	}
	
}
