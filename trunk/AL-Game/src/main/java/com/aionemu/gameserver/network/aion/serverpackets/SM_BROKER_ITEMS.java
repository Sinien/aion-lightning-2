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
package com.aionemu.gameserver.network.aion.serverpackets;

import java.nio.ByteBuffer;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.BrokerItem;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.stats.modifiers.SimpleModifier;
import com.aionemu.gameserver.model.gameobjects.stats.modifiers.StatModifier;
import com.aionemu.gameserver.model.items.ItemStone;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author kosyachok
 *
 */
public class SM_BROKER_ITEMS extends AionServerPacket
{
	private BrokerItem[] brokerItems;
	private int itemsCount;
	private int startPage;
	
	public SM_BROKER_ITEMS(BrokerItem[] brokerItems, int itemsCount, int startPage)
	{
		this.brokerItems = brokerItems;
		this.itemsCount = itemsCount;
		this.startPage = startPage;
	}
	
	@Override
	protected void writeImpl(AionConnection con, ByteBuffer buf)
	{	

		writeD(buf, itemsCount);
		writeC(buf, 0);
		writeH(buf, startPage);
		writeH(buf, brokerItems.length);
		
		for(BrokerItem item : brokerItems)
		{
			if(item.getItem().getItemTemplate().isArmor() || item.getItem().getItemTemplate().isWeapon())
				writeArmorWeaponInfo(buf, item);
			else
				writeCommonInfo(buf, item);
		}
	}
	
	private void writeArmorWeaponInfo(ByteBuffer buf, BrokerItem item)
	{
		writeD(buf, item.getItem().getObjectId());
		writeD(buf, item.getItem().getItemTemplate().getTemplateId());
		writeQ(buf, item.getPrice());
		writeQ(buf, item.getItem().getItemCount());
		writeC(buf, 0);
		writeC(buf, item.getItem().getEnchantLevel());
		writeD(buf, item.getItem().getItemSkinTemplate().getTemplateId());
		writeC(buf, 0);
		
		writeItemStones(buf, item.getItem());
		
		ItemStone god = item.getItem().getGodStone();
		writeD(buf, god == null ? 0 : god.getItemId());
		
		writeC(buf, 0);
		writeD(buf, 0);
		writeD(buf, 0);
		writeS(buf, item.getSeller());
		writeS(buf, ""); //creator
		
	}
	
	private void writeItemStones(ByteBuffer buf, Item item)
	{
		int count = 0;
		
		if(item.hasManaStones())
		{
			Set<ManaStone> itemStones = item.getItemStones();
			
			for(ManaStone itemStone : itemStones)
			{
				if(count == 6)
					break;

				StatModifier modifier = itemStone.getFirstModifier();
				if(modifier != null)
				{
					count++;
					writeC(buf, modifier.getStat().getItemStoneMask());
				}
			}
			writeB(buf, new byte[(6-count)]);
			count = 0;
			for(ManaStone itemStone : itemStones)
			{
				if(count == 6)
					break;

				StatModifier modifier = itemStone.getFirstModifier();
				if(modifier != null)
				{
					count++;
					writeH(buf, ((SimpleModifier)modifier).getValue());
				}
			}
			writeB(buf, new byte[(6-count)*2]);
		}
		else
		{
			writeB(buf, new byte[18]);
		}

		//for now max 6 stones - write some junk
	}
	
	private void writeCommonInfo(ByteBuffer buf, BrokerItem item)
	{
		writeD(buf, item.getItem().getObjectId());
		writeD(buf, item.getItem().getItemTemplate().getTemplateId());
		writeQ(buf, item.getPrice());
		writeQ(buf, item.getItem().getItemCount());
		writeD(buf, 0);
		writeD(buf, 0);
		writeD(buf, 0);
		writeD(buf, 0);
		writeD(buf, 0);
		writeD(buf, 0);
		writeD(buf, 0);
		writeD(buf, 0);
		writeD(buf, 0);
		writeH(buf, 0);
		writeS(buf, item.getSeller());
		writeS(buf, ""); //creator
	}
}
