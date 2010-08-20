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
package com.aionemu.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Storage;
import com.aionemu.gameserver.model.gameobjects.player.StorageType;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;

/**
 * @author ATracer
 */
public class ItemStorageTest
{
	List<Item>	itemList;

	@Before
	public void setup()
	{
		itemList = new ArrayList<Item>();

		for(int i = 0; i < 30; i++)
		{
			ItemTemplate template = new ItemTemplate();
			template.setItemId(i);
			Item item = new Item(0, i, template, 3, false, 2);
			itemList.add(item);
		}
	}

	@Test
	public void testStorageFull()
	{
		Storage inventory = new Storage(StorageType.CUBE);
		Assert.assertEquals(false, inventory.isFull());

		for(Item item : itemList)
		{
			inventory.putToBag(item);
		}
		Assert.assertEquals(true, inventory.isFull());
		Assert.assertEquals(28, inventory.getAllItems().size()); // cube + kinah
	}

}
