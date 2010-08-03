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
package com.aionemu.gameserver.skillengine.effect;

import java.util.concurrent.Future;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.alliance.PlayerAllianceMember;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MANTRA_EFFECT;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuraEffect")
public class AuraEffect extends EffectTemplate
{
	@XmlAttribute
	protected int	distance;
	@XmlAttribute(name = "skill_id")
	protected int	skillId;

	@Override
	public void applyEffect(Effect effect)
	{
		effect.addToEffectedController();
	}

	@Override
	public void calculate(Effect effect)
	{
		effect.addSucessEffect(this);
	}

	@Override
	public void onPeriodicAction(Effect effect)
	{
		Player effector = (Player) effect.getEffector();
		if (effector.isInAlliance())
		{
			for (PlayerAllianceMember allianceMember : effector.getPlayerAlliance().getMembersForGroup(effector.getObjectId()))
			{
				if (allianceMember.isOnline() && MathUtil.isIn3dRange(effector, allianceMember.getPlayer(), distance + 4))
				{
					applyAuraTo(allianceMember.getPlayer());
				}
			}
		}
		else if(effector.isInGroup())
		{
			for(Player member : effector.getPlayerGroup().getMembers())
			{
				if(MathUtil.isIn3dRange(effector, member, distance + 4))
				{
					applyAuraTo(member);
				}
			}
		}
		else
		{
			applyAuraTo(effector);
		}
	}
	
	/**
	 * 
	 * @param effector
	 */
	private void applyAuraTo(Player effector)
	{
		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);
		Effect e = new Effect(effector, effector, template, template.getLvl(), template.getEffectsDuration());
		e.initialize();
		e.applyEffect();
		PacketSendUtility.broadcastPacket(effector, new SM_MANTRA_EFFECT(effector, skillId));
	}

	@Override
	public void startEffect(final Effect effect)
	{
		Future<?> task = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Runnable(){

			@Override
			public void run()
			{
				onPeriodicAction(effect);
			}
		}, 0, 6500);
		effect.setPeriodicTask(task, position);
	}

	@Override
	public void endEffect(Effect effect)
	{
		// nothing todo
	}

}
