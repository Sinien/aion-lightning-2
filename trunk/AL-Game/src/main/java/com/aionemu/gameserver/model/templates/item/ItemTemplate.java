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
package com.aionemu.gameserver.model.templates.item;

import java.util.TreeSet;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.itemengine.actions.ItemActions;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.stats.modifiers.StatModifier;
import com.aionemu.gameserver.model.items.ItemId;
import com.aionemu.gameserver.model.items.ItemMask;
import com.aionemu.gameserver.model.templates.VisibleObjectTemplate;
import com.aionemu.gameserver.model.templates.itemset.ItemSetTemplate;
import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;

/**
 * @author Luno modified by ATracer
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "item_templates")
public class ItemTemplate extends VisibleObjectTemplate
{
	@XmlAttribute(name = "id", required = true)
	@XmlID
	private String				id;

	@XmlElement(name = "modifiers", required = false)
	protected ModifiersTemplate	modifiers;

	@XmlElement(name = "actions", required = false)
	protected ItemActions		actions;
	
	@XmlAttribute(name = "mask")
	private int					mask;

	@XmlAttribute(name = "slot")
	private int					itemSlot;
	
	@XmlAttribute(name = "usedelayid")
	private int					useDelayId;
	
	@XmlAttribute(name = "usedelay")
	private int					useDelay;

	@XmlAttribute(name = "equipment_type")
	private EquipType			equipmentType = EquipType.NONE;

	@XmlAttribute(name = "cash_item")
	private int					cashItem;

	@XmlAttribute(name = "dmg_decal")
	private int					dmgDecal;

	@XmlAttribute(name = "weapon_boost")
	private int					weaponBoost;

	@XmlAttribute(name = "price")
	private int					price;

	@XmlAttribute(name = "ap")
	private int					abyssPoints;

	@XmlAttribute(name = "ai")
	private int					abyssItem;

	@XmlAttribute(name = "aic")
	private int					abyssItemCount;

	@XmlAttribute(name = "max_stack_count")
	private int					maxStackCount;

	@XmlAttribute(name = "level")
	private int					level;

	@XmlAttribute(name = "quality")
	private ItemQuality			itemQuality;

	@XmlAttribute(name = "item_type")
	private String				itemType;					// TODO enum

	@XmlAttribute(name = "weapon_type")
	private WeaponType			weaponType;

	@XmlAttribute(name = "armor_type")
	private ArmorType			armorType;

	@XmlAttribute(name = "attack_type")
	private String				attackType;				// TODO enum

	@XmlAttribute(name = "attack_gap")
	private float				attackGap;					// TODO enum

	@XmlAttribute(name = "desc")
	private String				description;				// TODO string or int

	@XmlAttribute(name = "gender")
	private String				genderPermitted;			// enum

	@XmlAttribute(name = "option_slot_bonus")
	private int					optionSlotBonus;

	@XmlAttribute(name = "bonus_apply")
	private String				bonusApply;				// enum

	@XmlAttribute(name = "no_enchant")
	private boolean				noEnchant;

	@XmlAttribute(name = "can_proc_enchant")
	private boolean				canProcEnchant;

	@XmlAttribute(name = "can_split")
	private boolean				canSplit;

	@XmlAttribute(name = "drop")
	private boolean				itemDropPermitted;

	@XmlAttribute(name = "dye")
	private boolean				itemDyePermitted;

	@XmlAttribute(name = "race")
	private ItemRace			race	= ItemRace.ALL;

	private int					itemId;
	
	@XmlAttribute(name = "return_world")
	private int					returnWorldId;

	@XmlAttribute(name = "return_alias")
	private String				returnAlias;

	@XmlElement(name = "godstone")
	private GodstoneInfo		godstoneInfo;

	@XmlElement(name = "stigma")
	private Stigma				stigma;

	@XmlAttribute(name = "name")
	private String				name;

	@XmlAttribute(name = "restrict")
	private String				restrict;

	@XmlTransient
	private int[]				restricts;
	
	/**
	 * @return the mask
	 */
	public int getMask()
	{
		return mask;
	}

	public int getItemSlot()
	{
		return itemSlot;
	}
	
	/**
	 * 
	 * @param playerClass
	 * @return
	 */
	public boolean isClassSpecific(PlayerClass playerClass)
	{
		boolean related = restricts[playerClass.ordinal()] > 0;
		if(!related && !playerClass.isStartingClass())
		{
			related = restricts[PlayerClass.getStartingClassFor(playerClass).ordinal()] > 0;
		}
		return related;
	}
	
	/**
	 * 
	 * @param playerClass
	 * @param level
	 * @return
	 */
	public boolean isAllowedFor(PlayerClass playerClass, int level)
	{
		return restricts[playerClass.ordinal()] <= level && restricts[playerClass.ordinal()] != 0;
	}

	/**
	 * @return the modifiers
	 */
	public TreeSet<StatModifier> getModifiers()
	{
		if(modifiers != null)
		{
			return modifiers.getModifiers();
		}
		else
		{
			return null;
		}
	}

	/**
	 * @return the actions
	 */
	public ItemActions getActions()
	{
		return actions;
	}

	/**
	 * 
	 * @return the equipmentType
	 */
	public EquipType getEquipmentType()
	{
		return equipmentType;
	}

	/**
	 * @return the price
	 */
	public int getPrice()
	{
		return price;
	}

	/**
	 * @return the abyssPoints
	 */
	public int getAbyssPoints()
	{
		return abyssPoints;
	}

	/**
	 * @return the abyssItem
	 */
	public int getAbyssItem()
	{
		return abyssItem;
	}

	/**
	 * @return the abyssItemCount
	 */
	public int getAbyssItemCount()
	{
		return abyssItemCount;
	}

	/**
	 * @return the level
	 */
	public int getLevel()
	{
		return level;
	}

	/**
	 * @return the quality
	 */
	public ItemQuality getItemQuality()
	{
		return itemQuality;
	}

	/**
	 * @return the itemType
	 */
	public String getItemType()
	{
		return itemType;
	}

	/**
	 * @return the weaponType
	 */
	public WeaponType getWeaponType()
	{
		return weaponType;
	}

	/**
	 * @return the armorType
	 */
	public ArmorType getArmorType()
	{
		return armorType;
	}

	/**
	 * @return the description
	 */
	@Override
	public int getNameId()
	{
		try
		{
			int val = Integer.parseInt(description);
			return val;
		}
		catch(NumberFormatException nfe)
		{
			return 0;
		}
	}

	/**
	 * @return the cashItem
	 */
	public int getCashItem()
	{
		return cashItem;
	}

	/**
	 * @return the dmgDecal
	 */
	public int getDmgDecal()
	{
		return dmgDecal;
	}

	/**
	 * @return the maxStackCount
	 */
	public int getMaxStackCount()
	{
		return maxStackCount;
	}

	/**
	 * @return the attackType
	 */
	public String getAttackType()
	{
		return attackType;
	}

	/**
	 * @return the attackGap
	 */
	public float getAttackGap()
	{
		return attackGap;
	}

	/**
	 * @return the genderPermitted
	 */
	public String getGenderPermitted()
	{
		return genderPermitted;
	}

	/**
	 * @return the optionSlotBonus
	 */
	public int getOptionSlotBonus()
	{
		return optionSlotBonus;
	}

	/**
	 * @return the bonusApply
	 */
	public String getBonusApply()
	{
		return bonusApply;
	}

	/**
	 * @return the noEnchant
	 */
	public boolean isNoEnchant()
	{
		return noEnchant;
	}

	/**
	 * @return the canProcEnchant
	 */
	public boolean isCanProcEnchant()
	{
		return canProcEnchant;
	}

	/**
	 * @return the canSplit
	 */
	public boolean isCanSplit()
	{
		return canSplit;
	}

	/**
	 * @return the dyePermitted
	 */
	public boolean isItemDyePermitted()
	{
		return itemDyePermitted;
	}

	/**
	 * @return the itemDropPermitted
	 */
	public boolean isItemDropPermitted()
	{
		return itemDropPermitted;
	}

	/**
	 * @return the race
	 */
	public ItemRace getRace()
	{
		return race;
	}

	/**
	 * @return the weaponBoost
	 */
	public int getWeaponBoost()
	{
		return weaponBoost;
	}

	/**
	 * @return true or false
	 */
	public boolean isWeapon()
	{
		return equipmentType == EquipType.WEAPON;
	}

	/**
	 * @return true or false
	 */
	public boolean isArmor()
	{
		return equipmentType == EquipType.ARMOR;
	}

	public boolean isKinah()
	{
		return itemId == ItemId.KINAH.value();
	}
	
	public boolean isStigma()
	{
		return itemId > 140000000 && itemId < 140001000;
	}
	
	void afterUnmarshal (Unmarshaller u, Object parent)
	{
		setItemId(Integer.parseInt(id));
		String[] parts = restrict.split(",");
		restricts = new int[12];
		for(int i = 0; i < parts.length; i++)
		{
			restricts[i] = Integer.parseInt(parts[i]);
		}
	}

	public void setItemId(int itemId)
	{
		this.itemId = itemId;
	}

	/*
	 * @return id of the associated ItemSetTemplate or null if none
	 */
	public ItemSetTemplate getItemSet()
	{
		return DataManager.ITEM_SET_DATA.getItemSetTemplateByItemId(itemId);
	}
	
	/*
	 * Checks if the ItemTemplate belongs to an item set
	 */
	public boolean isItemSet()
	{
		return getItemSet() != null;
	}
	
	/**
	 * @return the godstoneInfo
	 */
	public GodstoneInfo getGodstoneInfo()
	{
		return godstoneInfo;
	}

	@Override
	public String getName()
	{
		return name != null ? name : StringUtils.EMPTY;
	}

	@Override
	public int getTemplateId()
	{
		return itemId;
	}

	/**
	 * @return the returnWorldId
	 */
	public int getReturnWorldId()
	{
		return returnWorldId;
	}

	/**
	 * @return the returnAlias
	 */
	public String getReturnAlias()
	{
		return returnAlias;
	}
	
	/**
	 * @return the delay for item.
	 */
	public int getDelayTime()
	{
		return useDelay;
	}
	
	/**
	 * @return item delay id
	 */
	public int getDelayId()
	{
		return useDelayId;
	}

	/**
	 * @return the stigma
	 */
	public Stigma getStigma()
	{
		return stigma;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSoulBound()
	{
		return (getMask() & ItemMask.SOUL_BOUND) == ItemMask.SOUL_BOUND;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isUndeletableQuestItem()
	{
		return (getMask() & ItemMask.UNDELETABLE_QUEST_ITEMS) == ItemMask.UNDELETABLE_QUEST_ITEMS;
	}
}
