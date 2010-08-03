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
package com.aionemu.gameserver.questEngine.handlers.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.template.ItemCollecting;

/**
 * @author MrPoke
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemCollectingData")
public class ItemCollectingData extends QuestScriptData
{

	@XmlAttribute(name = "start_npc_id", required = true)
	protected int	startNpcId;
	@XmlAttribute(name = "action_item_id")
	protected int	actionItemId;
    @XmlAttribute(name = "end_npc_id")
    protected int endNpcId;

	@Override
	public void register(QuestEngine questEngine)
	{
		ItemCollecting template = new ItemCollecting(id, startNpcId, actionItemId, endNpcId);
		questEngine.addQuestHandler(template);
	}

}
