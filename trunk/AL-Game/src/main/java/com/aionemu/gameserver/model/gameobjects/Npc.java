/*
 * This file is part of aion-emu <aion-emu.com>.
 *
 *  aion-emu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-emu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-emu.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.ai.npcai.AggressiveAi;
import com.aionemu.gameserver.ai.npcai.NpcAi;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.NpcMovementConfig;
import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.gameobjects.stats.NpcGameStats;
import com.aionemu.gameserver.model.gameobjects.stats.NpcLifeStats;
import com.aionemu.gameserver.model.templates.NpcTemplate;
import com.aionemu.gameserver.model.templates.VisibleObjectTemplate;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillList;
import com.aionemu.gameserver.model.templates.spawn.SpawnTemplate;
import com.aionemu.gameserver.model.templates.stats.NpcRank;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * This class is a base class for all in-game NPCs, what includes: monsters and npcs that player can talk to (aka
 * Citizens)
 * 
 * @author Luno
 * 
 */
public class Npc extends Creature
{
	
	private NpcSkillList npcSkillList;
	
	/**
	 * Constructor creating instance of Npc.
	 * 
	 * @param spawn
	 *            SpawnTemplate which is used to spawn this npc
	 * @param objId
	 *            unique objId
	 */
	public Npc(int objId, NpcController controller, SpawnTemplate spawnTemplate, VisibleObjectTemplate objectTemplate)
	{
		super(objId, controller, spawnTemplate, objectTemplate, new WorldPosition());
		controller.setOwner(this);
		
		super.setGameStats(new NpcGameStats(this));
		super.setLifeStats(new NpcLifeStats(this));
	}

	@Override
	public NpcTemplate getObjectTemplate()
	{
		return (NpcTemplate) objectTemplate;
	}
	@Override
	public String getName()
	{
		return getObjectTemplate().getName();
	}

	public int getNpcId()
	{
		return getObjectTemplate().getTemplateId();
	}

	@Override
	public byte getLevel()
	{
		return getObjectTemplate().getLevel();
	}

	/**
	 * @return the lifeStats
	 */
	@Override
	public NpcLifeStats getLifeStats()
	{
		return (NpcLifeStats) super.getLifeStats();
	}

	/**
	 * @return the gameStats
	 */
	@Override
	public NpcGameStats getGameStats()
	{
		return (NpcGameStats) super.getGameStats();
	}
		
	@Override
	public NpcController getController()
	{
		return (NpcController) super.getController();
	}

	public boolean hasWalkRoutes()
	{
		return getSpawn().getWalkerId() > 0 || (getSpawn().hasRandomWalk() && NpcMovementConfig.ACTIVE_NPC_MOVEMENT);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isAggressive()
	{
		String currentTribe = getObjectTemplate().getTribe();
		return DataManager.TRIBE_RELATIONS_DATA.hasAggressiveRelations(currentTribe) || isGuard() || isHostile();
	}
	
	public boolean isHostile()
	{
		String currentTribe = getObjectTemplate().getTribe();
		return DataManager.TRIBE_RELATIONS_DATA.hasHostileRelations(currentTribe);
	}
	
	@Override
	public boolean isAggressiveTo(Creature creature)
	{
		return creature.isAggroFrom(this) || creature.isHostileFrom(this);
	}
	
	@Override
	public boolean isAggroFrom(Npc npc)
	{
		return DataManager.TRIBE_RELATIONS_DATA.isAggressiveRelation(npc.getTribe(), getTribe());
	}
	
	@Override
	public boolean isHostileFrom(Npc npc)
	{
		return DataManager.TRIBE_RELATIONS_DATA.isHostileRelation(npc.getTribe(), getTribe());
	}

	@Override
	public boolean isSupportFrom(Npc npc)
	{
		return DataManager.TRIBE_RELATIONS_DATA.isSupportRelation(npc.getTribe(), getTribe());
	}

	/**
	 * 
	 * @return
	 */
	public boolean isGuard()
	{
		String currentTribe = getObjectTemplate().getTribe();
		return DataManager.TRIBE_RELATIONS_DATA.isGuardDark(currentTribe)
			|| DataManager.TRIBE_RELATIONS_DATA.isGuardLight(currentTribe);
	}
	
	@Override
	public String getTribe()
	{
		return this.getObjectTemplate().getTribe();
	}
	
	public int getAggroRange()
	{
		return getObjectTemplate().getAggroRange();
	}
	
	@Override
	public void initializeAi()
	{
		if(isAggressive() && !CustomConfig.DISABLE_MOB_AGGRO)
			this.ai = new AggressiveAi();
		else
			this.ai = new NpcAi();
		ai.setOwner(this);
	}

	/**
	 *  Check whether npc located at initial spawn location
	 *  
	 * @return true or false
	 */
	public boolean isAtSpawnLocation()
	{
		return MathUtil.getDistance(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(),
			getX(), getY(), getZ()) < 3 ;
	}

	/**
	 * @return the npcSkillList
	 */
	public NpcSkillList getNpcSkillList()
	{
		return npcSkillList;
	}

	/**
	 * @param npcSkillList the npcSkillList to set
	 */
	public void setNpcSkillList(NpcSkillList npcSkillList)
	{
		this.npcSkillList = npcSkillList;
	}
	
	@Override
	protected boolean isEnemyNpc(Npc visibleObject)
	{
		return false;//TODO
	}

	@Override
	protected boolean isEnemyPlayer(Player visibleObject)
	{
		return true;//TODO
	}
	
	@Override
	protected boolean isEnemySummon(Summon visibleObject)
	{
		return true;//TODO
	}
	
	@Override
	protected boolean canSeeNpc(Npc npc)
	{
		return true; //TODO
	}

	@Override
	protected boolean canSeePlayer(Player player)
	{
		if(!player.isInState(CreatureState.ACTIVE))
			return false;
		
		if (player.getVisualState() == 1 && getObjectTemplate().getRank() == NpcRank.NORMAL)
		   return false;
		
		if (player.getVisualState() == 2 && (getObjectTemplate().getRank() == NpcRank.ELITE || getObjectTemplate().getRank() == NpcRank.NORMAL))
		   return false;
		
		if (player.getVisualState() >= 3)
		   return false;
		
		return true;
	}
}
