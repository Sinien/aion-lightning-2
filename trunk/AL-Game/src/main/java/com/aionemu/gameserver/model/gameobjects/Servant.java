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
package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.ai.npcai.ServantAi;
import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.VisibleObjectTemplate;
import com.aionemu.gameserver.model.templates.spawn.SpawnTemplate;

/**
 * @author ATracer
 *
 */
public class Servant extends Npc
{
	/**
	 * Skill that will be used upon execution
	 */
	private int skillId;
	/**
	 * Creator of this trap.
	 */
	private Creature creator;
	/**
	 * Target of this servant
	 */
	private Creature target;
	/**
	 * Hp used on skill usage
	 */
	private int hpRatio;
	
	/**
	 * 
	 * @param objId
	 * @param controller
	 * @param spawnTemplate
	 * @param objectTemplate
	 */
	public Servant(int objId, NpcController controller, SpawnTemplate spawnTemplate, VisibleObjectTemplate objectTemplate)
	{
		super(objId, controller, spawnTemplate, objectTemplate);
	}

	/**
	 * @return the skillId
	 */
	public int getSkillId()
	{
		return skillId;
	}

	/**
	 * @param skillId the skillId to set
	 */
	public void setSkillId(int skillId)
	{
		this.skillId = skillId;
	}

	/**
	 * @return the creator
	 */
	public Creature getCreator()
	{
		return creator;
	}

	/**
	 * @param creator the creator to set
	 */
	public void setCreator(Creature creator)
	{
		this.creator = creator;
	}
	
	/**
	 * @return the target
	 */
	@Override
	public Creature getTarget()
	{
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(Creature target)
	{
		this.target = target;
	}
	
	/**
	 * @return the hpRatio
	 */
	public int getHpRatio()
	{
		return hpRatio;
	}

	/**
	 * @param hpRatio the hpRatio to set
	 */
	public void setHpRatio(int hpRatio)
	{
		this.hpRatio = hpRatio;
	}

	@Override
	public void initializeAi()
	{
		this.ai = new ServantAi();
		ai.setOwner(this);
	}
	
	@Override
	protected boolean isEnemyNpc(Npc visibleObject)
	{
		return this.creator.isEnemyNpc(visibleObject);
	}

	@Override
	protected boolean isEnemyPlayer(Player visibleObject)
	{
		return this.creator.isEnemyPlayer(visibleObject);
	}
	
	@Override
	protected boolean isEnemySummon(Summon summon)
	{
		return this.creator.isEnemySummon(summon);
	}

	/**
	 * @return NpcObjectType.TRAP
	 */
	@Override
	public NpcObjectType getNpcObjectType()
	{
		return NpcObjectType.SERVANT;
	}

	@Override
	public Creature getActingCreature()
	{
		return this.creator;
	}

	@Override
	public Creature getMaster()
	{
		return this.creator;
	}
}
