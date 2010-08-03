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
package com.aionemu.gameserver.services;

import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.EnchantsConfig;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Storage;
import com.aionemu.gameserver.model.gameobjects.stats.StatEnum;
import com.aionemu.gameserver.model.gameobjects.stats.id.EnchantStatEffectId;
import com.aionemu.gameserver.model.gameobjects.stats.listeners.ItemEquipmentListener;
import com.aionemu.gameserver.model.gameobjects.stats.modifiers.AddModifier;
import com.aionemu.gameserver.model.gameobjects.stats.modifiers.RateModifier;
import com.aionemu.gameserver.model.gameobjects.stats.modifiers.StatModifier;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.templates.item.ArmorType;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.WeaponType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATS_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UPDATE_ITEM;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 * 
 */
public class EnchantService
{
	private static final Logger	log	= Logger.getLogger(EnchantService.class);

	/**
	 * @param player
	 * @param targetItem
	 * @param parentItem
	 */
	public static void breakItem(Player player, Item targetItem, Item parentItem)
	{
		Storage inventory = player.getInventory();

		ItemTemplate itemTemplate = targetItem.getItemTemplate();
		ItemQuality quality = itemTemplate.getItemQuality();

		int number = 0;
		int level = 0;
		switch(quality)
		{
			case COMMON:
			case JUNK:
				number = Rnd.get(1, 2);
				level = Rnd.get(0, 5);
				break;
			case RARE:
				number = Rnd.get(1, 3);
				level = Rnd.get(0, 10);
				break;
			case LEGEND:
			case MYTHIC:
				number = Rnd.get(1, 3);
				level = Rnd.get(0, 15);
				break;
			case EPIC:
			case UNIQUE:
				number = Rnd.get(1, 3);
				level = Rnd.get(0, 20);
				break;
		}

		int enchantItemLevel = targetItem.getItemTemplate().getLevel() + level;
		int enchantItemId = 166000000 + enchantItemLevel;

		inventory.removeFromBag(targetItem, true);
		PacketSendUtility.sendPacket(player, new SM_DELETE_ITEM(targetItem.getObjectId()));

		inventory.removeFromBagByObjectId(parentItem.getObjectId(), 1);

		ItemService.addItem(player, enchantItemId, number);
	}

	/**
	 * @param player
	 * @param parentItem
	 * @param targetItem
	 */
	public static boolean enchantItem(Player player, Item parentItem, Item targetItem)
	{
		int enchantStoneLevel = parentItem.getItemTemplate().getLevel();
		int targetItemLevel = targetItem.getItemTemplate().getLevel();

		if(targetItemLevel > enchantStoneLevel)
			return false;

		int qualityCap = 0;

		ItemQuality quality = targetItem.getItemTemplate().getItemQuality();

		switch(quality)
		{
			case COMMON:
			case JUNK:
				qualityCap = 0;
				break;
			case RARE:
				qualityCap = 5;
				break;
			case LEGEND:
			case MYTHIC:
				qualityCap = 10;
				break;
			case EPIC:
			case UNIQUE:
				qualityCap = 15;
				break;
		}

		int success = 50;

		int levelDiff = enchantStoneLevel - targetItemLevel;

		int extraSuccess = levelDiff - qualityCap;
		if(extraSuccess > 0)
		{
			success += extraSuccess * 5;
		}

		if(success >= 95)
			success = 95;

		boolean result = false;

		if(Rnd.get(0, 100) < success)
			result = true;

		int currentEnchant = targetItem.getEnchantLevel();

		if(!result)
		{
			// Retail: http://powerwiki.na.aiononline.com/aion/Patch+Notes:+1.9.0.1
			// When socketing fails at +11~+15, the value falls back to +10.
			if (currentEnchant > 10)
				currentEnchant = 10;
			else if (currentEnchant > 0)
				currentEnchant -= 1;
		}
		else
		{
			// Items that are Fabled or Eternal can get up to +15.
			ItemQuality targetQuality = targetItem.getItemTemplate().getItemQuality();
			if (targetQuality == ItemQuality.UNIQUE || targetQuality == ItemQuality.EPIC)
			{
				if (currentEnchant < 15)
					currentEnchant += 1;
			}
			else
			{
				if (currentEnchant < 10)
					currentEnchant += 1;
			}
		}

		if(targetItem.isEquipped())
			onItemUnequip(player, targetItem);

		targetItem.setEnchantLevel(currentEnchant);

		if(targetItem.isEquipped())
			onItemEquip(player, targetItem);

		PacketSendUtility.sendPacket(player, new SM_UPDATE_ITEM(targetItem));

		if(targetItem.isEquipped())
			player.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
		else
			player.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);

		if(result)
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ENCHANT_ITEM_SUCCEED(new DescriptionId(Integer
				.parseInt(targetItem.getName()))));
		}
		else
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ENCHANT_ITEM_FAILED(new DescriptionId(Integer
				.parseInt(targetItem.getName()))));
		}
		player.getInventory().removeFromBagByObjectId(parentItem.getObjectId(), 1);

		return result;
	}

	/**
	 * @param player
	 * @param parentItem
	 * @param targetItem
	 */
	public static boolean socketManastone(Player player, Item parentItem, Item targetItem)
	{
		boolean result = false;
		int successRate = 76;

		int stoneCount = targetItem.getItemStones().size();
		switch(stoneCount)
		{
			case 1:
				successRate = EnchantsConfig.MSPERCENT;
				break;
			case 2:
				successRate = EnchantsConfig.MSPERCENT1;
				break;
			case 3:
				successRate = EnchantsConfig.MSPERCENT2;
				break;
			case 4:
				successRate = EnchantsConfig.MSPERCENT3;
				break;
			case 5:
				successRate = EnchantsConfig.MSPERCENT4;
				break;
		}

		if(stoneCount >= 6)
			successRate = EnchantsConfig.MSPERCENT5;

		if(Rnd.get(0, 100) < successRate)
			result = true;
		if(result)
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_OPTION_SUCCEED(new DescriptionId(
				Integer.parseInt(targetItem.getName()))));
			ManaStone manaStone = ItemService.addManaStone(targetItem, parentItem.getItemTemplate().getTemplateId());
			if(targetItem.isEquipped())
			{
				ItemEquipmentListener.addStoneStats(manaStone, player.getGameStats());
				PacketSendUtility.sendPacket(player, new SM_STATS_INFO(player));
			}
		}
		else
		{
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_OPTION_FAILED(new DescriptionId(
				Integer.parseInt(targetItem.getName()))));
			Set<ManaStone> manaStones = targetItem.getItemStones();
			if(targetItem.isEquipped())
			{
				ItemEquipmentListener.removeStoneStats(manaStones, player.getGameStats());
				PacketSendUtility.sendPacket(player, new SM_STATS_INFO(player));
			}
			ItemService.removeAllManastone(player, targetItem);
		}

		PacketSendUtility.sendPacket(player, new SM_UPDATE_ITEM(targetItem));
		player.getInventory().removeFromBagByObjectId(parentItem.getObjectId(), 1);
		return result;
	}

	/**
	 * 
	 * @param player
	 * @param item
	 */
	public static void onItemEquip(Player player, Item item)
	{
		try
		{
			int enchantLevel = item.getEnchantLevel();

			if(enchantLevel == 0)
				return;

			boolean isWeapon = item.getItemTemplate().isWeapon();
			boolean isArmor = item.getItemTemplate().isArmor();
			if(isWeapon)
			{
				TreeSet<StatModifier> modifiers = getWeaponModifiers(item);

				if(modifiers == null)
					return;

				EnchantStatEffectId statId = EnchantStatEffectId.getInstance(item.getObjectId(), item
					.getEquipmentSlot());

				player.getGameStats().addModifiers(statId, modifiers);
				return;
			}

			if(isArmor)
			{
				TreeSet<StatModifier> modifiers = getArmorModifiers(item);

				if(modifiers == null)
					return;

				EnchantStatEffectId statId = EnchantStatEffectId.getInstance(item.getObjectId(), item
					.getEquipmentSlot());
				player.getGameStats().addModifiers(statId, modifiers);
			}
		}
		catch(Exception ex)
		{
			log.error(ex.getCause() != null ? ex.getCause().getMessage() : "Error on item equip.");
		}
	}
	
	/**
	 * 
	 * @param player
	 * @param item
	 */
	public static void onItemUnequip(Player player, Item item)
	{
		try
		{
			int enchantLevel = item.getEnchantLevel();

			if(enchantLevel == 0)
				return;

			EnchantStatEffectId statId = EnchantStatEffectId.getInstance(item.getObjectId(), item.getEquipmentSlot());

			if(player.getGameStats().effectAlreadyAdded(statId))
				player.getGameStats().endEffect(statId);

		}
		catch(Exception ex)
		{
			log.error(ex.getCause() != null ? ex.getCause().getMessage() : null);
		}
	}

	/**
	 * 
	 * @param item
	 * @return
	 */
	private static TreeSet<StatModifier> getArmorModifiers(Item item)
	{
		TreeSet<StatModifier> modifiers = null;

		ArmorType armorType = item.getItemTemplate().getArmorType();
		if(armorType == null)
			return null;

		int enchantLevel = item.getEnchantLevel();

		switch(armorType)
		{
			case ROBE:
				switch(item.getEquipmentSlot())
				{
					case 1 << 3: // torso
						modifiers = EnchantWeapon.DEF3.getModifiers(enchantLevel);
						break;
					case 1 << 5: // boots
						modifiers = EnchantWeapon.DEF1.getModifiers(enchantLevel);
						break;
					case 1 << 11: // pauldrons
						modifiers = EnchantWeapon.DEF1.getModifiers(enchantLevel);
						break;
					case 1 << 12: // pants
						modifiers = EnchantWeapon.DEF2.getModifiers(enchantLevel);
						break;
					case 1 << 4: // gloves
						modifiers = EnchantWeapon.DEF1.getModifiers(enchantLevel);
						break;
				}
				break;
			case LEATHER:
				switch(item.getEquipmentSlot())
				{
					case 1 << 3: // torso
						modifiers = EnchantWeapon.DEF6.getModifiers(enchantLevel);
						break;
					case 1 << 5: // boots
						modifiers = EnchantWeapon.DEF4.getModifiers(enchantLevel);
						break;
					case 1 << 11: // pauldrons
						modifiers = EnchantWeapon.DEF4.getModifiers(enchantLevel);
						break;
					case 1 << 12: // pants
						modifiers = EnchantWeapon.DEF5.getModifiers(enchantLevel);
						break;
					case 1 << 4: // gloves
						modifiers = EnchantWeapon.DEF4.getModifiers(enchantLevel);
						break;
				}
				break;
			case CHAIN:
				switch(item.getEquipmentSlot())
				{
					case 1 << 3: // torso
						modifiers = EnchantWeapon.DEF9.getModifiers(enchantLevel);
						break;
					case 1 << 5: // boots
						modifiers = EnchantWeapon.DEF7.getModifiers(enchantLevel);
						break;
					case 1 << 11: // pauldrons
						modifiers = EnchantWeapon.DEF7.getModifiers(enchantLevel);
						break;
					case 1 << 12: // pants
						modifiers = EnchantWeapon.DEF8.getModifiers(enchantLevel);
						break;
					case 1 << 4: // gloves
						modifiers = EnchantWeapon.DEF7.getModifiers(enchantLevel);
						break;
				}
				break;
			case PLATE:
				switch(item.getEquipmentSlot())
				{
					case 1 << 3: // torso
						modifiers = EnchantWeapon.DEF12.getModifiers(enchantLevel);
						break;
					case 1 << 5: // boots
						modifiers = EnchantWeapon.DEF10.getModifiers(enchantLevel);
						break;
					case 1 << 11: // pauldrons
						modifiers = EnchantWeapon.DEF10.getModifiers(enchantLevel);
						break;
					case 1 << 12: // pants
						modifiers = EnchantWeapon.DEF11.getModifiers(enchantLevel);
						break;
					case 1 << 4: // gloves
						modifiers = EnchantWeapon.DEF10.getModifiers(enchantLevel);
						break;
				}
				break;
		}
		return modifiers;
	}

	/**
	 * 
	 * @param item
	 * @return
	 */
	private static TreeSet<StatModifier> getWeaponModifiers(Item item)
	{
		WeaponType weaponType = item.getItemTemplate().getWeaponType();

		if(weaponType == null)
			return null;

		int enchantLevel = item.getEnchantLevel();

		TreeSet<StatModifier> modifiers = null;
		switch(weaponType)
		{
			case BOOK_2H:
				modifiers = EnchantWeapon.SPELLBOOK.getModifiers(enchantLevel);
				break;
			case DAGGER_1H:
				modifiers = EnchantWeapon.DAGGER.getModifiers(enchantLevel);
				break;
			case BOW:
				modifiers = EnchantWeapon.BOW.getModifiers(enchantLevel);
				break;
			case ORB_2H:
				modifiers = EnchantWeapon.ORB.getModifiers(enchantLevel);
				break;
			case STAFF_2H:
				modifiers = EnchantWeapon.STAFF.getModifiers(enchantLevel);
				break;
			case SWORD_1H:
				modifiers = EnchantWeapon.SWORD.getModifiers(enchantLevel);
				break;
			case SWORD_2H:
				modifiers = EnchantWeapon.GREATSWORD.getModifiers(enchantLevel);
				break;
			case MACE_1H:
				modifiers = EnchantWeapon.MACE.getModifiers(enchantLevel);
				break;
			case POLEARM_2H:
				modifiers = EnchantWeapon.POLEARM.getModifiers(enchantLevel);
				break;
		}
		return modifiers;
	}

	/**
	 * @author ATracer
	 */
	private enum EnchantWeapon
	{
		DAGGER()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_ATTACK, 2 * level, true));
				return mod;
			}
		},

		SWORD()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_ATTACK, 2 * level, true));
				return mod;
			}
		},

		GREATSWORD()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_ATTACK, 4 * level, true));
				return mod;
			}
		},

		POLEARM()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_ATTACK, 4 * level, true));
				return mod;
			}
		},

		BOW()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_ATTACK, 4 * level, true));
				return mod;
			}
		},

		MACE()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_ATTACK, 3 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.BOOST_MAGICAL_SKILL, 20 * level, true));
				return mod;
			}
		},

		STAFF()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_ATTACK, 3 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.BOOST_MAGICAL_SKILL, 20 * level, true));
				return mod;
			}
		},

		SPELLBOOK()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_ATTACK, 3 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.BOOST_MAGICAL_SKILL, 20 * level, true));
				return mod;
			}
		},

		ORB()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_ATTACK, 3 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.BOOST_MAGICAL_SKILL, 20 * level, true));
				return mod;
			}
		},

		SHIELD()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(RateModifier.newInstance(StatEnum.BLOCK, 2 * level, true));
				return mod;
			}
		},

		DEF1()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_DEFENSE, 1 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.MAXHP, 10 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.CRITICAL_RESIST, 2 * level, true));
				return mod;
			}
		},

		DEF2()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_DEFENSE, 2 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.MAXHP, 12 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.CRITICAL_RESIST, 3 * level, true));
				return mod;
			}
		},

		DEF3()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_DEFENSE, 3 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.MAXHP, 14 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.CRITICAL_RESIST, 4 * level, true));
				return mod;
			}
		},

		DEF4()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_DEFENSE, 3 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.MAXHP, 8 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.CRITICAL_RESIST, 2 * level, true));
				return mod;
			}
		},

		DEF5()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_DEFENSE, 5 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.MAXHP, 10 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.CRITICAL_RESIST, 3 * level, true));
				return mod;
			}
		},

		DEF6()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_DEFENSE, 4 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.MAXHP, 12 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.CRITICAL_RESIST, 4 * level, true));
				return mod;
			}
		},
		
		DEF7()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_DEFENSE, 3 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.MAXHP, 6 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.CRITICAL_RESIST, 2 * level, true));
				return mod;
			}
		},
		
		DEF8()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_DEFENSE, 4 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.MAXHP, 8 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.CRITICAL_RESIST, 3 * level, true));
				return mod;
			}
		},
		
		DEF9()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_DEFENSE, 5 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.MAXHP, 10 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.CRITICAL_RESIST, 4 * level, true));
				return mod;
			}
		},
		
		DEF10()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_DEFENSE, 4 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.MAXHP, 4 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.CRITICAL_RESIST, 2 * level, true));
				return mod;
			}
		},
		
		DEF11()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_DEFENSE, 5 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.MAXHP, 6 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.CRITICAL_RESIST, 3 * level, true));
				return mod;
			}
		},
		
		DEF12()
		{
			@Override
			public TreeSet<StatModifier> getModifiers(int level)
			{
				TreeSet<StatModifier> mod = new TreeSet<StatModifier>();
				mod.add(AddModifier.newInstance(StatEnum.PHYSICAL_DEFENSE, 6 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.MAXHP, 8 * level, true));
				mod.add(AddModifier.newInstance(StatEnum.CRITICAL_RESIST, 4 * level, true));
				return mod;
			}
		};

		public abstract TreeSet<StatModifier> getModifiers(int level);

	}
}