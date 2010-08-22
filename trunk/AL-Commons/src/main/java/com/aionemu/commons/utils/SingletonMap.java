/*
 * This file is part of aion-lightning <aion-lightning.com>.
 *
 *  aion-lightning is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-lightning is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-lightning.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.commons.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;

/**
 * @author NB4L1
 */
public final class SingletonMap<K, V> implements Map<K, V>
{
	private boolean		initialized	= false;
	private Map<K, V>	map			= AECollections.emptyMap();

	private boolean		shared		= false;

	@SuppressWarnings("deprecation")
	private void init()
	{
		if(!initialized)
		{
			synchronized(this)
			{
				if(!initialized)
				{
					map = new FastMap<K, V>().setShared(shared);
					initialized = true;
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public SingletonMap<K, V> setShared()
	{
		shared = true;

		synchronized(this)
		{
			if(initialized)
			{
				((FastMap<K, V>) map).setShared(true);
			}
		}

		return this;
	}

	@Override
	public void clear()
	{
		map.clear();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return map.containsValue(value);
	}

	@Override
	public Set<Entry<K, V>> entrySet()
	{
		return map.entrySet();
	}

	@Override
	public V get(Object key)
	{
		return map.get(key);
	}

	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public Set<K> keySet()
	{
		return map.keySet();
	}

	@Override
	public V put(K key, V value)
	{
		init();

		return map.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		init();

		map.putAll(m);
	}

	@Override
	public V remove(Object key)
	{
		return map.remove(key);
	}

	@Override
	public int size()
	{
		return map.size();
	}

	@Override
	public Collection<V> values()
	{
		return map.values();
	}

	@Override
	public String toString()
	{
		return super.toString() + "-" + map.toString();
	}
}
