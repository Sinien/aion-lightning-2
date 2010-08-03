/*
 * This file is part of aion-unique <aion-unique.org>.
 *
 * aion-unique is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aion-unique is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.questEngine.handlers.models.ItemCollectingData;
import com.aionemu.gameserver.questEngine.handlers.models.MonsterHuntData;
import com.aionemu.gameserver.questEngine.handlers.models.QuestScriptData;
import com.aionemu.gameserver.questEngine.handlers.models.ReportToData;
import com.aionemu.gameserver.questEngine.handlers.models.WorkOrdersData;
import com.aionemu.gameserver.questEngine.handlers.models.XmlQuestData;

/**
 * @author MrPoke
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "quest_scripts")
public class QuestScriptsData
{
	@XmlElements( { @XmlElement(name = "report_to", type = ReportToData.class),
		@XmlElement(name = "monster_hunt", type = MonsterHuntData.class),
		@XmlElement(name = "xml_quest", type = XmlQuestData.class),
		@XmlElement(name = "item_collecting", type = ItemCollectingData.class),
		@XmlElement(name = "work_order", type = WorkOrdersData.class)
		})
	protected List<QuestScriptData>	data;

	/**
	 * @return the data
	 */
	public List<QuestScriptData> getData()
	{
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(List<QuestScriptData> data)
	{
		this.data = data;
	}
}
