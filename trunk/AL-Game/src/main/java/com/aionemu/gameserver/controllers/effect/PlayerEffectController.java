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
package com.aionemu.gameserver.controllers.effect;

import java.util.Collections;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.alliance.PlayerAllianceEvent;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.group.GroupEvent;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABNORMAL_STATE;
import com.aionemu.gameserver.services.AllianceService;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 *
 */
public class PlayerEffectController extends EffectController
{
	/**
	 * weapon mastery
	 */
	private int weaponEffects;
	
	/**
	 * armor mastery
	 */
	private int armorEffects;
	
	/**
	 * current food effect
	 */
	private Effect foodEffect;

	public PlayerEffectController(Creature owner)
	{
		super(owner);
	}
	
	@Override
	public void addEffect(Effect effect)
	{
		if(effect.isFood())
			addFoodEffect(effect);
		
		if(checkDuelCondition(effect))
			return;
		
		super.addEffect(effect);
		updatePlayerIconsAndGroup(effect);
	}
	
	@Override
	public void clearEffect(Effect effect)
	{
		if(effect.isFood())
			foodEffect = null;
	
		super.clearEffect(effect);
		updatePlayerIconsAndGroup(effect);
	}

	@Override
	public Player getOwner()
	{
		return (Player) super.getOwner();
	}

	/**
	 * @param effect
	 */
	private void updatePlayerIconsAndGroup(Effect effect)
	{
		if(!effect.isPassive())
		{
			updatePlayerEffectIcons();		
			if(getOwner().isInGroup())
				getOwner().getPlayerGroup().updateGroupUIToEvent(getOwner(), GroupEvent.UPDATE);
			if(getOwner().isInAlliance())
				AllianceService.getInstance().updateAllianceUIToEvent(getOwner(), PlayerAllianceEvent.UPDATE);
		}
	}
	
	/**
	 * Effect of DEBUFF should not be added if duel ended (friendly unit)
	 * @param effect
	 * @return
	 */
	private boolean checkDuelCondition(Effect effect)
	{
		Creature creature = effect.getEffector();
		if(creature instanceof Player)
		{
			if(getOwner().isFriend((Player) creature) && effect.getTargetSlot() == SkillTargetSlot.DEBUFF.ordinal())
				return true;
		}
		
		return false;
	}
	
	/**
	 * @param effect
	 */
	private void addFoodEffect(Effect effect)
	{
		if(foodEffect != null)
			foodEffect.endEffect();
		foodEffect = effect;
	}

	/**
	 * Weapon mastery
	 */
	public void setWeaponMastery(int skillId)
	{
		weaponEffects = skillId;
	}

	public void unsetWeaponMastery()
	{
		weaponEffects = 0;
	}

	public int getWeaponMastery()
	{
		return weaponEffects;
	}
	
	public boolean isWeaponMasterySet(int skillId)
	{
		return weaponEffects == skillId;
	}
	
	/**
	 * Armor mastery
	 */
	public void setArmorMastery(int skillId)
	{
		armorEffects = skillId;
	}

	public void unsetArmorMastery()
	{
		armorEffects = 0;
	}

	public int getArmorMastery()
	{
		return armorEffects;
	}
	
	public boolean isArmorMasterySet(int skillId)
	{
		return armorEffects == skillId;
	}

	/**
	 * @param skillId
	 * @param skillLvl
	 * @param currentTime
	 * @param reuseDelay
	 */
	public void addSavedEffect(int skillId, int skillLvl, int currentTime)
	{
		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);
		int duration = template.getEffectsDuration();
		int remainingTime = duration - currentTime;
		
		if(remainingTime <= 0)
			return;
		
		Effect effect = new Effect(getOwner(), getOwner(), template, skillLvl, remainingTime);
		if(effect.isFood())
			addFoodEffect(effect);
		abnormalEffectMap.put(effect.getStack(), effect);
		effect.addAllEffectToSucess();
		effect.startEffect(true);
		
		PacketSendUtility.sendPacket(getOwner(),
			new SM_ABNORMAL_STATE(Collections.singletonList(effect), abnormals));
		
	}
	
}
