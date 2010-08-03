/*
 * This file is part of aion-unique <aion-unique.smfnew.com>.
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
package com.aionemu.gameserver.utils.stats;

import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.FallDamageConfig;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.model.SkillElement;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Equipment;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.gameobjects.stats.CreatureGameStats;
import com.aionemu.gameserver.model.gameobjects.stats.StatEnum;
import com.aionemu.gameserver.model.gameobjects.stats.modifiers.SimpleModifier;
import com.aionemu.gameserver.model.gameobjects.stats.modifiers.StatModifier;
import com.aionemu.gameserver.model.templates.item.WeaponType;
import com.aionemu.gameserver.model.templates.stats.NpcRank;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.aionemu.gameserver.services.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 * @author alexa026
 */
public class StatFunctions
{
	private static Logger log = Logger.getLogger(StatFunctions.class);

	/**
	 * 
	 * @param player
	 * @param target
	 * @return XP reward from target
	 */
	public static long calculateSoloExperienceReward(Player player, Creature target)
	{
		int playerLevel = player.getCommonData().getLevel();
		int targetLevel = target.getLevel();

		int baseXP = ((Npc)target).getObjectTemplate().getStatsTemplate().getMaxXp();
		int xpPercentage =  XPRewardEnum.xpRewardFrom(targetLevel - playerLevel);

		return (int) Math.floor(baseXP * xpPercentage * player.getRates().getXpRate() / 100);
	}

	/**
	 * 
	 * @param player
	 * @param target
	 * @return
	 */
	public static long calculateGroupExperienceReward(int maxLevelInRange, Creature target)
	{
		int targetLevel = target.getLevel();

		int baseXP = ((Npc)target).getObjectTemplate().getStatsTemplate().getMaxXp();
		int xpPercentage =  XPRewardEnum.xpRewardFrom(targetLevel - maxLevelInRange);

		return (int) Math.floor(baseXP * xpPercentage / 100);
	}

	/**
	 * ref: http://www.aionsource.com/forum/mechanic-analysis/42597-character-stats-xp-dp-origin-gerbator-team-july-2009-a.html
	 * 
	 * @param player
	 * @param target
	 * @return DP reward from target
	 */

	public static int calculateSoloDPReward(Player player, Creature target) 
	{
		int playerLevel = player.getCommonData().getLevel();
		int targetLevel = target.getLevel();
		NpcRank npcRank = ((Npc) target).getObjectTemplate().getRank();

		//TODO: fix to see monster Rank level, NORMAL lvl 1, 2 | ELITE lvl 1, 2 etc..
		//look at: http://www.aionsource.com/forum/mechanic-analysis/42597-character-stats-xp-dp-origin-gerbator-team-july-2009-a.html
		int baseDP = targetLevel * calculateRankMultipler(npcRank);

		int xpPercentage =  XPRewardEnum.xpRewardFrom(targetLevel - playerLevel);
		return (int) Math.floor(baseDP * xpPercentage * player.getRates().getXpRate() / 100);

	}
	
	/**
	 * 
	 * @param player
	 * @param target
	 * @return AP reward
	 */
	public static int calculateSoloAPReward(Player player, Creature target) 
	{
		int playerLevel = player.getCommonData().getLevel();
		int targetLevel = target.getLevel();								
		int percentage =  XPRewardEnum.xpRewardFrom(targetLevel - playerLevel);
		return (int) Math.floor(10 * percentage * player.getRates().getApNpcRate() / 100);
	}
	
	/**
	 * 
	 * @param maxLevelInRange
	 * @param target
	 * @return
	 */
	public static int calculateGroupAPReward(int maxLevelInRange, Creature target)
	{
		int targetLevel = target.getLevel();
		NpcRank npcRank = ((Npc) target).getObjectTemplate().getRank();								

		//TODO: fix to see monster Rank level, NORMAL lvl 1, 2 | ELITE lvl 1, 2 etc..
		int baseAP = 10 + calculateRankMultipler(npcRank) - 1;

		int apPercentage =  XPRewardEnum.xpRewardFrom(targetLevel - maxLevelInRange);

		return (int) Math.floor(baseAP * apPercentage / 100);
	}
	
	/**
	 * 
	 * @param defeated
	 * @param winner
	 * @return Points Lost in PvP Death
	 */
	public static int calculatePvPApLost(Player defeated, Player winner)
	{
		int pointsLost = Math.round(defeated.getAbyssRank().getRank().getPointsLost() * defeated.getRates().getApPlayerRate());

		// Level penalty calculation
		int difference = winner.getLevel() - defeated.getLevel();

		if(difference > 4)
		{
			pointsLost = Math.round(pointsLost * 0.1f);
		}
		else
		{
			switch(difference)
			{
				case 3:
					pointsLost = Math.round(pointsLost * 0.85f);
					break;
				case 4:
					pointsLost = Math.round(pointsLost * 0.65f);
					break;
			}
		}
		return pointsLost;
	}

	/**
	 * 
	 * @param defeated
	 * @param winner
	 * @return Points Gained in PvP Kill
	 */
	public static int calculatePvpApGained(Player defeated, int maxRank, int maxLevel)
	{
		int pointsGained = Math.round(defeated.getAbyssRank().getRank().getPointsGained());

		// Level penalty calculation
		int difference = maxLevel - defeated.getLevel();

		if(difference > 4)
		{
			pointsGained = Math.round(pointsGained * 0.1f);
		}
		else if(difference < -3)
		{
			pointsGained = Math.round(pointsGained * 1.3f);
		}
		else
		{
			switch(difference)
			{
				case 3:
					pointsGained = Math.round(pointsGained * 0.85f);
					break;
				case 4:
					pointsGained = Math.round(pointsGained * 0.65f);
					break;
				case -2:
					pointsGained = Math.round(pointsGained * 1.1f);
					break;
				case -3:
					pointsGained = Math.round(pointsGained * 1.2f);
					break;
			}
		}

		// Abyss rank penalty calculation
		int winnerAbyssRank = maxRank;
		int defeatedAbyssRank = defeated.getAbyssRank().getRank().getId();
		int abyssRankDifference = winnerAbyssRank - defeatedAbyssRank;

		if(winnerAbyssRank <= 7 && abyssRankDifference > 0)
		{
			float penaltyPercent = abyssRankDifference * 0.05f;			

			pointsGained -= Math.round(pointsGained * penaltyPercent);
		}

		return pointsGained;
	}
	
	/**
	 * 
	 * @param player
	 * @param target
	 * @return DP reward
	 */
	public static int calculateGroupDPReward(Player player, Creature target)
	{
		int playerLevel = player.getCommonData().getLevel();
		int targetLevel = target.getLevel();
		NpcRank npcRank = ((Npc) target).getObjectTemplate().getRank();								

		//TODO: fix to see monster Rank level, NORMAL lvl 1, 2 | ELITE lvl 1, 2 etc..
		int baseDP = targetLevel * calculateRankMultipler(npcRank);

		int xpPercentage =  XPRewardEnum.xpRewardFrom(targetLevel - playerLevel);

		return (int) Math.floor(baseDP * xpPercentage * player.getRates().getGroupXpRate() / 100);
	}	
	
	/**
	 * Hate based on BOOST_HATE stat
	 * Now used only from skills, probably need to use for regular attack
	 * 
	 * @param creature
	 * @param value
	 * @return
	 */
	public static int calculateHate(Creature creature, int value) 
	{
		return Math.round(value * creature.getGameStats().getCurrentStat(StatEnum.BOOST_HATE) / 100f);
	}

	/**
	 * 
	 * @param player
	 * @param target
	 * @return Damage made to target (-hp value)
	 */
	public static int calculateBaseDamageToTarget(Creature attacker, Creature target)
	{
		return calculatePhysicDamageToTarget(attacker, target, 0);
	}

	/**
	 * 
	 * @param player
	 * @param target
	 * @param skillDamages
	 * @return Damage made to target (-hp value)
	 */
	public static int calculatePhysicDamageToTarget(Creature attacker, Creature target, int skillDamages)
	{
		CreatureGameStats<?> ags = attacker.getGameStats();
		CreatureGameStats<?> tgs = target.getGameStats();

		int resultDamage = 0;

		if (attacker instanceof Player)
		{
			int totalMin = ags.getCurrentStat(StatEnum.MIN_DAMAGES);
			int totalMax = ags.getCurrentStat(StatEnum.MAX_DAMAGES);
			int average = Math.round((totalMin + totalMax)/2);
			int mainHandAttack = ags.getBaseStat(StatEnum.MAIN_HAND_POWER);

			Equipment equipment = ((Player)attacker).getEquipment();

			WeaponType weaponType = equipment.getMainHandWeaponType();

			if(weaponType != null)
			{
				if(average < 1)
				{
					average = 1;
					log.warn("Weapon stat MIN_MAX_DAMAGE resulted average zero in main-hand calculation");
					log.warn("Weapon ID: " + String.valueOf(equipment.getMainHandWeapon().getItemTemplate().getTemplateId()));
					log.warn("MIN_DAMAGE = " + String.valueOf(totalMin));
					log.warn("MAX_DAMAGE = " + String.valueOf(totalMax));
				}
				
				//TODO move to controller
				if(weaponType == WeaponType.BOW)
					equipment.useArrow();
				
				if(equipment.getMainHandWeapon().hasFusionedItem())
				{
					Item fusionedItem = ItemService.newItem(equipment.getMainHandWeapon().getFusionedItem(), 1);
					if(fusionedItem != null)
					{
						TreeSet<StatModifier> fusionedModifiers = fusionedItem.getItemTemplate().getModifiers();
						for(StatModifier sm : fusionedModifiers)
						{
							if(sm.getStat() == StatEnum.MAIN_HAND_POWER && sm instanceof SimpleModifier)
							{
								SimpleModifier mod = (SimpleModifier)sm;
								mainHandAttack += Math.round(mod.getValue() / 10);
							}
						}
					}
				}

				int min = Math.round((((mainHandAttack * 100)/ average) * totalMin)/100);
				int max = Math.round((((mainHandAttack * 100)/ average) * totalMax)/100);

				int base = Rnd.get(min,max);
				
				
				
				resultDamage = Math.round((base * (ags.getCurrentStat(StatEnum.POWER) * 0.01f + (ags.getBaseStat(StatEnum.MAIN_HAND_POWER) * 0.2f)* 0.01f)) 
						                  + ags.getStatBonus(StatEnum.MAIN_HAND_POWER) + skillDamages);

			}
			else   //if hand attack
			{
				int base = Rnd.get(16,20);
				resultDamage = Math.round(base * (ags.getCurrentStat(StatEnum.POWER) * 0.01f));
			}

			//adjusting baseDamages according to attacker and target level
			//
			resultDamage = adjustDamages(attacker, target, resultDamage);

			if(attacker.isInState(CreatureState.POWERSHARD))
			{
				Item mainHandPowerShard = equipment.getMainHandPowerShard();
				if(mainHandPowerShard != null)
				{
					resultDamage += mainHandPowerShard.getItemTemplate().getWeaponBoost();

					equipment.usePowerShard(mainHandPowerShard, 1);
				}
			}
		}
		else if(attacker instanceof Summon)
		{
			int baseDamage = ags.getCurrentStat(StatEnum.MAIN_HAND_POWER);
			int max = (baseDamage + baseDamage * attacker.getLevel() / 10);
			int min = max - ags.getCurrentStat(StatEnum.MAIN_HAND_POWER);		
			resultDamage += Rnd.get(min, max);
		}
		else
		{
			NpcRank npcRank = ((Npc) attacker).getObjectTemplate().getRank();
			double multipler = calculateRankMultipler(npcRank);
			double hpGaugeMod = 1+(((Npc) attacker).getObjectTemplate().getHpGauge()/10);
			int baseDamage = ags.getCurrentStat(StatEnum.MAIN_HAND_POWER);
			int max = (int)((baseDamage * multipler * hpGaugeMod) + ((baseDamage*attacker.getLevel())/10));
			int min = max - ags.getCurrentStat(StatEnum.MAIN_HAND_POWER);		
			resultDamage += Rnd.get(min, max);
		}

		resultDamage -= Math.round(tgs.getCurrentStat(StatEnum.PHYSICAL_DEFENSE) * 0.10f);

		if (resultDamage<=0)
			resultDamage=1;

		return resultDamage;
	}

	/**
	 * 
	 * @param attacker
	 * @param target
	 * @return
	 */
	public static int calculateOffHandPhysicDamageToTarget(Creature attacker, Creature target)
	{
		CreatureGameStats<?> ags = attacker.getGameStats();
		CreatureGameStats<?> tgs = target.getGameStats();

		int totalMin = ags.getCurrentStat(StatEnum.MIN_DAMAGES);
		int totalMax = ags.getCurrentStat(StatEnum.MAX_DAMAGES);
		int average = Math.round((totalMin + totalMax)/2);
		int offHandAttack = ags.getBaseStat(StatEnum.OFF_HAND_POWER);

		Equipment equipment = ((Player)attacker).getEquipment();
		
		if(average < 1)
		{
			average = 1;
			log.warn("Weapon stat MIN_MAX_DAMAGE resulted average zero in off-hand calculation");
			log.warn("Weapon ID: " + String.valueOf(equipment.getOffHandWeapon().getItemTemplate().getTemplateId()));
			log.warn("MIN_DAMAGE = " + String.valueOf(totalMin));
			log.warn("MAX_DAMAGE = " + String.valueOf(totalMax));
		}

		int Damage = 0;
		int min = Math.round((((offHandAttack * 100)/ average) * totalMin)/100);
		int max = Math.round((((offHandAttack * 100)/ average) * totalMax)/100);

		int base = Rnd.get(min,max);
		Damage = Math.round((base * (ags.getCurrentStat(StatEnum.POWER) * 0.01f + (ags.getBaseStat(StatEnum.OFF_HAND_POWER) * 0.2f) * 0.01f)) 
                 + ags.getStatBonus(StatEnum.OFF_HAND_POWER));

		Damage = adjustDamages(attacker, target, Damage);

		if(attacker.isInState(CreatureState.POWERSHARD))
		{
			Item offHandPowerShard = equipment.getOffHandPowerShard();
			if(offHandPowerShard != null)
			{
				Damage += offHandPowerShard.getItemTemplate().getWeaponBoost();
				equipment.usePowerShard(offHandPowerShard, 1);
			}
		}

		Damage -= Math.round(tgs.getCurrentStat(StatEnum.PHYSICAL_DEFENSE) * 0.10f);
		
		for(float i = 0.25f; i <= 1; i+=0.25f)
		{
			if(Rnd.get(0, 100) < 50)
			{
				Damage *= i;
				break;
			}
		}

		if (Damage<=0)
			Damage=1;

		return Damage;
	}


	/**
	 * @param player
	 * @param target
	 * @param skillEffectTemplate
	 * @return HP damage to target
	 */
	public static int calculateMagicDamageToTarget(Creature speller, Creature target, int baseDamages, SkillElement element)
	{
		CreatureGameStats<?> sgs = speller.getGameStats();
		CreatureGameStats<?> tgs = target.getGameStats();
		
		int totalBoostMagicalSkill = 0;
		
		if(speller instanceof Player && ((Player)speller).getEquipment().getMainHandWeapon() != null && ((Player)speller).getEquipment().getMainHandWeapon().hasFusionedItem())
		{
			Item fusionedItem = ItemService.newItem(((Player)speller).getEquipment().getMainHandWeapon().getFusionedItem(), 1);
			if(fusionedItem != null)
			{
				TreeSet<StatModifier> fusionedModifiers = fusionedItem.getItemTemplate().getModifiers();
				for(StatModifier sm : fusionedModifiers)
				{
					if(sm.getStat() == StatEnum.BOOST_MAGICAL_SKILL && sm instanceof SimpleModifier)
					{
						SimpleModifier mod = (SimpleModifier)sm;
						totalBoostMagicalSkill += Math.round(mod.getValue() / 10);
					}
				}
			}
		}

		totalBoostMagicalSkill += sgs.getCurrentStat(StatEnum.BOOST_MAGICAL_SKILL);
		
		int damages = Math.round(baseDamages * ((sgs.getCurrentStat(StatEnum.KNOWLEDGE) / 100f) + (totalBoostMagicalSkill / 1000f)));
		
		//adjusting baseDamages according to attacker and target level
		//
		damages = adjustDamages(speller, target, damages);

		// element resist: fire, wind, water, eath
		//
		// 10 elemental resist ~ 1% reduce of magical baseDamages
		//
		damages = Math.round(damages * (1 - tgs.getMagicalDefenseFor(element) / 1000f));

		// IMPORTANT NOTES
		//
		// magicalResistance supposed to be counted to EVADE magic, not to reduce damage, only the elementaryDefense it's counted to reduce magic attack
		//
		//     so example if 200 magic resist vs 100 magic accuracy, 200 - 100 = 100/10 = 0.10 or 10% chance of EVADE
		//
		// damages -= Math.round((elementaryDefense+magicalResistance)*0.60f);

		if (damages<=0) {
			damages=1;
		}

		return damages;
	}

	/**
	 * 
	 * @param npcRank
	 * @return
	 */
	public static int calculateRankMultipler(NpcRank npcRank)
	{
		//FIXME: to correct formula, have any reference?
		int multipler;
		switch(npcRank) 
		{
			case JUNK: 
				multipler = 2;
				break;
			case NORMAL: 
				multipler = 2;
				break;
			case ELITE:
				multipler = 3;
				break;
			case HERO: 
				multipler = 4;
				break;
			case LEGENDARY: 
				multipler = 5;
				break;
			default: 
				multipler = 1;
		}

		return multipler;
	}

	/**
	 * adjust baseDamages according to their level || is PVP?
	 *
	 * @ref:
	 *
	 * @param attacker lvl
	 * @param target lvl
	 * @param baseDamages
	 *
	 **/
	public static int adjustDamages(Creature attacker, Creature target, int Damages) {

		int attackerLevel = attacker.getLevel();
		int targetLevel = target.getLevel();
		int baseDamages = Damages;

		//fix this for better monster target condition please
		if ( (attacker instanceof Player) && !(target instanceof Player)) {

			if(targetLevel > attackerLevel) {

				float multipler = 0.0f;
				int differ = (targetLevel - attackerLevel);

				if( differ <= 2 ) {
					return baseDamages;
				}
				else if( differ > 2 && differ < 10 ) {
					multipler = (differ - 2f) / 10f;
					baseDamages -= Math.round((baseDamages * multipler));
				}

				else {
					baseDamages -= Math.round((baseDamages * 0.80f));
				}

				return baseDamages;
			}
		} //end of damage to monster

		//PVP damages is capped of 60% of the actual baseDamage
		else if( (attacker instanceof Player) && (target instanceof Player) ) {
			baseDamages = Math.round(baseDamages * 0.60f);
			float pvpAttackBonus = attacker.getGameStats().getCurrentStat(StatEnum.PVP_ATTACK_RATIO) * 0.001f;
			float pvpDefenceBonus = target.getGameStats().getCurrentStat(StatEnum.PVP_DEFEND_RATIO) * 0.001f;
			baseDamages = Math.round(baseDamages + (baseDamages * pvpAttackBonus) - (baseDamages * pvpDefenceBonus));
			return baseDamages;
		}

		return baseDamages;

	}

	/**
	 *  Calculates DODGE chance
	 *  
	 * @param attacker
	 * @param attacked
	 * @return int
	 */
	public static int calculatePhysicalDodgeRate(Creature attacker, Creature attacked)
	{
		//check always dodge
		if(attacked.getObserveController().checkAttackStatus(AttackStatus.DODGE))
			return 100;
		
		int accuracy;

		if(attacker instanceof Player && ((Player) attacker).getEquipment().getOffHandWeaponType() != null)
			accuracy = Math.round((attacker.getGameStats().getCurrentStat(StatEnum.MAIN_HAND_ACCURACY) + attacker
				.getGameStats().getCurrentStat(StatEnum.OFF_HAND_ACCURACY)) / 2);
		else
			accuracy = attacker.getGameStats().getCurrentStat(StatEnum.MAIN_HAND_ACCURACY);

		int dodgeRate = (attacked.getGameStats().getCurrentStat(StatEnum.EVASION) - accuracy) / 10;
		// maximal dodge rate
		if(dodgeRate > 30)
			dodgeRate = 30;

		if(dodgeRate <= 0)
			return 1;

		return dodgeRate;
	}
	
	/**
	 *  Calculates PARRY chance
	 *  
	 * @param attacker
	 * @param attacked
	 * @return int
	 */
	public static int calculatePhysicalParryRate(Creature attacker, Creature attacked)
	{
		//check always parry
		if(attacked.getObserveController().checkAttackStatus(AttackStatus.PARRY))
			return 100;
		
		int accuracy;
		
		if(attacker instanceof Player && ((Player) attacker).getEquipment().getOffHandWeaponType() != null)
			accuracy = Math.round((attacker.getGameStats().getCurrentStat(StatEnum.MAIN_HAND_ACCURACY) + attacker
				.getGameStats().getCurrentStat(StatEnum.OFF_HAND_ACCURACY)) / 2);
		else
			accuracy = attacker.getGameStats().getCurrentStat(StatEnum.MAIN_HAND_ACCURACY);

		int parryRate = (attacked.getGameStats().getCurrentStat(StatEnum.PARRY) - accuracy) / 10;
		// maximal parry rate
		if(parryRate > 40)
			parryRate = 40;

		if(parryRate <= 0)
			return 1;

		return parryRate;
	}
	
	/**
	 *  Calculates BLOCK chance
	 *  
	 * @param attacker
	 * @param attacked
	 * @return int
	 */
	public static int calculatePhysicalBlockRate(Creature attacker, Creature attacked)
	{
		//check always block
		if(attacked.getObserveController().checkAttackStatus(AttackStatus.BLOCK))
			return 100;
		
		int accuracy;

		if(attacker instanceof Player && ((Player) attacker).getEquipment().getOffHandWeaponType() != null)
			accuracy = Math.round((attacker.getGameStats().getCurrentStat(StatEnum.MAIN_HAND_ACCURACY) + attacker
				.getGameStats().getCurrentStat(StatEnum.OFF_HAND_ACCURACY)) / 2);
		else
			accuracy = attacker.getGameStats().getCurrentStat(StatEnum.MAIN_HAND_ACCURACY);

		int blockRate = (attacked.getGameStats().getCurrentStat(StatEnum.BLOCK) - accuracy) / 10;
		// maximal block rate
		if(blockRate > 50)
			blockRate = 50;

		if(blockRate <= 0)
			return 1;

		return blockRate;
	}
	
	/**
	 *  Calculates CRITICAL chance
	 *  
	 * @param attacker
	 * @return double
	 */
	public static double calculatePhysicalCriticalRate(Creature attacker, Creature attacked)
	{
		int critical;

		if(attacker instanceof Player && ((Player) attacker).getEquipment().getOffHandWeaponType() != null)
			critical = Math.round(((attacker.getGameStats().getCurrentStat(StatEnum.MAIN_HAND_CRITICAL) + attacker
				.getGameStats().getCurrentStat(StatEnum.OFF_HAND_CRITICAL)) / 2) - attacked.getGameStats().getCurrentStat(StatEnum.CRITICAL_RESIST)); 
		else
			critical = attacker.getGameStats().getCurrentStat(StatEnum.MAIN_HAND_CRITICAL) - attacked.getGameStats().getCurrentStat(StatEnum.CRITICAL_RESIST); 
			
		
		double criticalRate;

		if(critical <= 440)
			criticalRate = critical * 0.1f;
		else if(critical <= 600)
			criticalRate = (440 * 0.1f) + ((critical - 440) * 0.05f);
		else
			criticalRate = (440 * 0.1f) + (160 * 0.05f) + ((critical - 600) * 0.02f);
		// minimal critical rate
		if(criticalRate < 1)
			criticalRate = 1;

		return criticalRate;
	}
	
	/**
	 *  Calculates RESIST chance
	 *  
	 * @param attacker
	 * @param attacked
	 * @return int
	 */
	public static int calculateMagicalResistRate(Creature attacker, Creature attacked)
	{		
		if(attacked.getObserveController().checkAttackStatus(AttackStatus.RESIST))
			return 100;
		
		int resistRate = Math.round((attacked.getGameStats().getCurrentStat(StatEnum.MAGICAL_RESIST) - attacker
			.getGameStats().getCurrentStat(StatEnum.MAGICAL_ACCURACY)) / 10);

		return resistRate;
	}

	/**
	 * Calculates the fall damage
	 * 
	 * @param player
	 * @param distance
	 * @return True if the player is forced to his bind location.
	 */
	public static boolean calculateFallDamage(Player player, float distance)
	{
		if(player.isInvul())
		{
			return false;
		}

		if(distance >= FallDamageConfig.MAXIMUM_DISTANCE_DAMAGE)
		{
			player.getController().onStopMove();
			player.getFlyController().onStopGliding();
			player.getController().onDie(player);

			player.getReviveController().bindRevive();
			return true;
		}
		else if(distance >= FallDamageConfig.MINIMUM_DISTANCE_DAMAGE)
		{
			float dmgPerMeter = player.getLifeStats().getMaxHp() * FallDamageConfig.FALL_DAMAGE_PERCENTAGE / 100f;
			int damage = (int) (distance * dmgPerMeter);

			player.getLifeStats().reduceHp(damage, player);
			PacketSendUtility.sendPacket(player, new SM_ATTACK_STATUS(player, SM_ATTACK_STATUS.TYPE.DAMAGE, 0, damage));
		}

		return false;
	}

}
