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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Monster;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Skill;


/**
 * 
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FirstTargetProperty")
public class FirstTargetProperty
    extends Property
{

    @XmlAttribute(required = true)
    protected FirstTargetAttribute value;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link FirstTargetAttribute }
     *     
     */
    public FirstTargetAttribute getValue() {
        return value;
    }
    
    @Override
	public boolean set(Skill skill)
	{
		switch(value)
		{
			case ME:
				skill.setFirstTarget(skill.getEffector());
				break;			
			case TARGETORME:
				if(skill.getFirstTarget() == null)
					skill.setFirstTarget(skill.getEffector());
				else if (skill.getFirstTarget() instanceof Monster)
				{
					Monster monsterEffected = (Monster)skill.getFirstTarget();
					Player playerEffector = (Player)skill.getEffector();
					if ( monsterEffected.isEnemy(playerEffector) )
						skill.setFirstTarget(skill.getEffector());
				}
				else if ( (skill.getFirstTarget() instanceof Player) && (skill.getEffector() instanceof Player) )
				{
					Player playerEffected = (Player)skill.getFirstTarget();
					Player playerEffector = (Player)skill.getEffector();
					if ( playerEffected.getCommonData().getRace().getRaceId() != playerEffector.getCommonData().getRace().getRaceId() )
						skill.setFirstTarget(skill.getEffector());
				}
				else if (skill.getFirstTarget() instanceof Npc)
				{
					Npc npcEffected = (Npc)skill.getFirstTarget();
					Player playerEffector = (Player)skill.getEffector();
					if ( npcEffected.isEnemy(playerEffector) )
						skill.setFirstTarget(skill.getEffector());
				}
				else if ( (skill.getFirstTarget() instanceof Summon) && (skill.getEffector() instanceof Player) )
				{
					Summon summon = (Summon)skill.getFirstTarget();
					Player playerEffected = summon.getMaster();
					Player playerEffector = (Player)skill.getEffector();
					if ( playerEffected.getCommonData().getRace().getRaceId() != playerEffector.getCommonData().getRace().getRaceId() )
						skill.setFirstTarget(skill.getEffector());
				}
				break;
			case TARGET:
				if(skill.getFirstTarget() == null)
					return false;
				break;
			case MYPET:
				Creature effector = skill.getEffector();
				if(effector instanceof Player)
				{
					Summon summon = ((Player)effector).getSummon();
					if(summon != null)
						skill.setFirstTarget(summon);
					else
						return false;
				}
				else
				{
					return false;
				}
				break;
			case PASSIVE:
				skill.setFirstTarget(skill.getEffector());
				break;
			case POINT:
				// TODO: Implement Range Check for Point 
				skill.setFirstTargetRangeCheck(false);
				return true;
		}

		if(skill.getFirstTarget() != null)
			skill.getEffectedList().add(skill.getFirstTarget());
		return true;
	}
}
