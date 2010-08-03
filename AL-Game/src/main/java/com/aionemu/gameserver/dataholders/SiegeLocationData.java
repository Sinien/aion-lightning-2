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
package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javolution.util.FastMap;

import com.aionemu.gameserver.model.siege.Artifact;
import com.aionemu.gameserver.model.siege.Commander;
import com.aionemu.gameserver.model.siege.Fortress;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeLocationTemplate;

/**
 * @author Sarynth
 */
@XmlRootElement(name = "siege_locations")
@XmlAccessorType(XmlAccessType.FIELD)
public class SiegeLocationData
{
	@XmlElement(name = "siege_location")
	private List<SiegeLocationTemplate> siegeLocationTemplates;
	
	/**
	 *  Map that contains skillId - SkillTemplate key-value pair
	 */
	private FastMap<Integer, SiegeLocation> siegeLocations = new FastMap<Integer, SiegeLocation>();

	void afterUnmarshal(Unmarshaller u, Object parent)
	{
		siegeLocations.clear();
		for (SiegeLocationTemplate template : siegeLocationTemplates)
		{
			switch(template.getType())
			{
				case FORTRESS:
					siegeLocations.put(template.getId(), new Fortress(template));
					break;
				case ARTIFACT:
					siegeLocations.put(template.getId(), new Artifact(template));
					break;
				case BOSSRAID_LIGHT:
				case BOSSRAID_DARK:
					siegeLocations.put(template.getId(), new Commander(template));
					break;
				default:
					break;
			}
		}
	}
	
	public int size()
	{
		return siegeLocations.size();
	}
	
	public FastMap<Integer, SiegeLocation> getSiegeLocations()
	{
		return siegeLocations;
	}
}
