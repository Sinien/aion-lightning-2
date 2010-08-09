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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABNORMAL_EFFECT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABNORMAL_STATE;
import com.aionemu.gameserver.skillengine.effect.EffectId;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;
import com.aionemu.gameserver.skillengine.model.SkillType;
import com.aionemu.gameserver.taskmanager.tasks.PacketBroadcaster.BroadcastMode;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 *
 */
public class EffectController
{
	private Creature owner;

	protected Map<String, Effect> passiveEffectMap = new FastMap<String, Effect>().shared();
	protected Map<String, Effect> noshowEffects = new FastMap<String, Effect>().shared();
	protected Map<String, Effect> abnormalEffectMap = new FastMap<String, Effect>().shared();

	protected int abnormals;

	public EffectController(Creature owner)
	{
		this.owner = owner;
	}

	/**
	 * @return the owner
	 */
	public Creature getOwner()
	{
		return owner;
	}

	/**
	 * 
	 * @param effect
	 */
	public void addEffect(Effect effect)
	{
		Map<String, Effect> mapToUpdate = getMapForEffect(effect);

		Effect existingEffect = mapToUpdate.get(effect.getStack());
		if(existingEffect != null)
		{
			// check stack level
			if(existingEffect.getSkillStackLvl() > effect.getSkillStackLvl())
				return;
			// check skill level (when stack level same)
			if(existingEffect.getSkillStackLvl() == effect.getSkillStackLvl()
				&& existingEffect.getSkillLevel() > effect.getSkillLevel())
				return;

			existingEffect.endEffect();
		}

		if(effect.isToggle() && mapToUpdate.size() >= 3)
		{
			Iterator<Effect> iter = mapToUpdate.values().iterator();
			Effect nextEffect = iter.next();
			nextEffect.endEffect();
			iter.remove();
		}

		mapToUpdate.put(effect.getStack(), effect);
		effect.startEffect(false);

		if(!effect.isPassive())
		{
			broadCastEffects();
		}
	}

	/**
	 * 
	 * @param effect
	 * @return
	 */
	private Map<String, Effect> getMapForEffect(Effect effect)
	{
		if(effect.isPassive())
			return passiveEffectMap;

		if(effect.isToggle())
			return noshowEffects;

		return abnormalEffectMap;
	}

	/**
	 * 
	 * @param stack
	 * @return abnormalEffectMap
	 */
	public Effect getAnormalEffect(String stack)
	{
		return abnormalEffectMap.get(stack);
	}

	public void broadCastEffects()
	{
		owner.addPacketBroadcastMask(BroadcastMode.BROAD_CAST_EFFECTS);
	}

	/**
	 *  Broadcasts current effects to all visible objects
	 */
	public void broadCastEffectsImp()
	{
		List<Effect> effects = getAbnormalEffects();
		PacketSendUtility.broadcastPacket(getOwner(),
			new SM_ABNORMAL_EFFECT(getOwner().getObjectId(), abnormals, effects));
	}

	/**
	 *  Used when player see new player
	 *  
	 * @param player
	 */
	public void sendEffectIconsTo(Player player)
	{
		List<Effect> effects = getAbnormalEffects();
		PacketSendUtility.sendPacket(player, new SM_ABNORMAL_EFFECT(getOwner().getObjectId(),
			abnormals, effects));
	}

	/**
	 * 
	 * @param effect
	 */
	public void clearEffect(Effect effect)
	{
		abnormalEffectMap.remove(effect.getStack());
		broadCastEffects();
	}

	/**
	 * Removes the effect by skillid.
	 * 
	 * @param skillid
	 */
	public void removeEffect(int skillid)
	{
		for(Effect effect : abnormalEffectMap.values()){
			if(effect.getSkillId()==skillid){
				effect.endEffect();
				abnormalEffectMap.remove(effect.getStack());
			}
		}
	}
	
	/**
	 * Removes the effect by SkillSetException Number.
	 * 
	 * @param SkillSetException Number
	 */
	public void removeEffectBySetNumber(final int setNumber)
	{
		for(Effect effect : abnormalEffectMap.values())
		{
			if(effect.getSkillSetException() == setNumber)
			{
				effect.endEffect();
				abnormalEffectMap.remove(effect.getStack());
			}
		}
		for(Effect effect : passiveEffectMap.values())
		{
			if(effect.getSkillSetException() == setNumber)
			{
				effect.endEffect();
				passiveEffectMap.remove(effect.getStack());
			}
		}
		for(Effect effect : noshowEffects.values())
		{
			if(effect.getSkillSetException() == setNumber)
			{
				effect.endEffect();
				noshowEffects.remove(effect.getStack());
			}
		}
	}
	
	/**
	 * Removes the effect with SkillSetException Reserved Number (aka 1).
	 * 
	 */
	public void removeEffectWithSetNumberReserved()
	{
		removeEffectBySetNumber(1);
	}

	/**
	 * 
	 * @param effectId
	 */
	public void removeEffectByEffectId(int effectId)
	{
		for(Effect effect : abnormalEffectMap.values()){
			if(effect.containsEffectId(effectId)){
				effect.endEffect();
				abnormalEffectMap.remove(effect.getStack());
			}
		}
	}
	
	/**
	 * 
	 * @param targetSlot
	 * @param count
	 */
	public void removeEffectByTargetSlot(SkillTargetSlot targetSlot, int count)
	{
		for(Effect effect : abnormalEffectMap.values())
		{
			if(count == 0)
				break;
			
			if(effect.getTargetSlot() == targetSlot.ordinal())
			{
				effect.endEffect();
				abnormalEffectMap.remove(effect.getStack());
				count--;
			}
		}
	}

	/**
	 * @param skillType
	 * @param value
	 */
	public void removeEffectBySkillType(SkillType skillType, int value)
	{
		for(Effect effect : abnormalEffectMap.values())
		{
			if(value == 0)
				break;

			if(effect.getSkillType() == skillType)
			{
				effect.endEffect();
				abnormalEffectMap.remove(effect.getStack());
				value--;
			}
		}
	}
	
	/**
	 * 
	 * @param skillType
	 * @param targetSlot
	 * @param value
	 */
	public void removeEffectBySkillTypeAndTargetSlot(SkillType skillType, SkillTargetSlot targetSlot, int value)
	{
		for(Effect effect : abnormalEffectMap.values())
		{
			if(value == 0)
				break;

			if(effect.getSkillType() == skillType && effect.getTargetSlot() == targetSlot.ordinal())
			{
				effect.endEffect();
				abnormalEffectMap.remove(effect.getStack());
				value--;
			}
		}
	}

	/**
	 * Removes the effect by skillid.
	 * 
	 * @param skillid
	 */
	public void removePassiveEffect(int skillid)
	{
		for(Effect effect : passiveEffectMap.values()){
			if(effect.getSkillId()==skillid){
				effect.endEffect();
				passiveEffectMap.remove(effect.getStack());
			}
		}
	}

	/**
	 * 
	 * @param skillid
	 */
	public void removeNoshowEffect(int skillid)
	{
		for(Effect effect : noshowEffects.values()){
			if(effect.getSkillId()==skillid){
				effect.endEffect();
				noshowEffects.remove(effect.getStack());
			}
		}
	}

	/**
	 * @see TargetSlot
	 * @param targetSlot
	 */
	public void removeAbnormalEffectsByTargetSlot(SkillTargetSlot targetSlot)
	{
		for(Effect effect : abnormalEffectMap.values()){
			if(effect.getTargetSlot() == targetSlot.ordinal()){
				effect.endEffect();
				abnormalEffectMap.remove(effect.getStack());
			}
		}
	}
	/**
	 * Removes all effects from controllers and ends them appropriately
	 * Passive effect will not be removed
	 */
	public void removeAllEffects()
	{
		for(Effect effect : abnormalEffectMap.values())
		{
			effect.endEffect();
		}
		abnormalEffectMap.clear();
		for(Effect effect : noshowEffects.values())
		{
			effect.endEffect();
		}
		noshowEffects.clear();
	}

	public void updatePlayerEffectIcons()
	{
		getOwner().addPacketBroadcastMask(BroadcastMode.UPDATE_PLAYER_EFFECT_ICONS);
	}

	public void updatePlayerEffectIconsImpl()
	{
		List<Effect> effects = getAbnormalEffects();

		PacketSendUtility.sendPacket((Player) owner,
			new SM_ABNORMAL_STATE(effects, abnormals));
	}

	/**
	 * @return copy of anbornals list
	 */
	public List<Effect> getAbnormalEffects()
	{
		List<Effect> effects = new ArrayList<Effect>();
		Iterator<Effect> iterator = iterator();
		while(iterator.hasNext())
		{
			Effect effect = iterator.next();
			if(effect != null)
				effects.add(effect);
		}
		return effects;
	}

	/**
	 *  ABNORMAL EFFECTS
	 */

	public void setAbnormal(int mask)
	{
		abnormals |= mask;
	}

	public void unsetAbnormal(int mask)
	{
		abnormals &= ~mask;
	}

	/**
	 *  Used for checking unique abnormal states
	 *  
	 * @param effectId
	 * @return
	 */
	public boolean isAbnoramlSet(EffectId effectId)
	{
		return (abnormals & effectId.getEffectId()) == effectId.getEffectId();
	}

	/**
	 *  Used for compound abnormal state checks
	 *  
	 * @param effectId
	 * @return
	 */
	public boolean isAbnormalState(EffectId effectId)
	{
		int state = abnormals & effectId.getEffectId();
		return state > 0 && state <= effectId.getEffectId();
	}

	public int getAbnormals()
	{
		return abnormals;
	}

	/**
	 * 
	 * @return
	 */
	public Iterator<Effect> iterator()
	{
		return abnormalEffectMap.values().iterator();
	}
}
