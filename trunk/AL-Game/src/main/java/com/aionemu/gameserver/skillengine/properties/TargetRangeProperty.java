/*
 * This file is part of aion-unique <aion-unique.com>.
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
package com.aionemu.gameserver.skillengine.properties;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import com.aionemu.gameserver.model.alliance.PlayerAllianceMember;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Trap;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.MathUtil;


/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TargetRangeProperty")
public class TargetRangeProperty
extends Property
{

	private static final Logger log = Logger.getLogger(TargetRangeProperty.class);
	
	@XmlAttribute(required = true)
	protected TargetRangeAttribute value;

	@XmlAttribute
	protected int distance;
	
	@XmlAttribute
	protected int maxcount;

	/**
	 * Gets the value of the value property.
	 *     
	 */
	public TargetRangeAttribute getValue() {
		return value;
	}

	@Override
	public boolean set(Skill skill)
	{
		List<Creature> effectedList = skill.getEffectedList();
		int counter = 0;
		switch(value)
		{
			case ONLYONE:
				break;			
			case AREA:	
				Creature firstTarget = skill.getFirstTarget();
				if(firstTarget == null)
				{
					log.warn("CHECKPOINT: first target is null for skillid " + skill.getSkillTemplate().getSkillId());
					return false;
				}
			
				for(VisibleObject nextCreature : firstTarget.getKnownList().getKnownObjects().values())
				{
					if(counter >= maxcount)
						break;

					//firstTarget is already added, look: FirstTargetProperty
					if(firstTarget == nextCreature)
						continue;
					
					//TODO this is a temporary hack for traps
					if(skill.getEffector() instanceof Trap && ((Trap) skill.getEffector()).getCreator() == nextCreature)
						continue;
					
					//TODO: here value +4 till better move controller developed
					if(nextCreature instanceof Creature 
						&& MathUtil.isIn3dRange(firstTarget, nextCreature, distance + 4))
					{
						effectedList.add((Creature) nextCreature);
						counter++;
					}
				}
				break;
			case PARTY:
				if(skill.getEffector() instanceof Player)
				{
					Player effector = (Player)skill.getEffector();
					if (effector.isInAlliance())
					{
						effectedList.clear();
						for(PlayerAllianceMember allianceMember : effector.getPlayerAlliance().getMembersForGroup(effector.getObjectId()))
						{
							if (!allianceMember.isOnline()) continue;
							Player member = allianceMember.getPlayer();
							if(MathUtil.isIn3dRange(effector, member, distance + 4))
								effectedList.add(member);
						}
					}
					else if (effector.isInGroup())
					{
						effectedList.clear();
						for(Player member : effector.getPlayerGroup().getMembers())
						{
							//TODO: here value +4 till better move controller developed
							if(member != null && MathUtil.isIn3dRange(effector, member, distance + 4))
								effectedList.add(member);
						}
					}
				}
				break;
			case NONE:
				break;
			
			//TODO other enum values
		}
		return true;
	}
}
