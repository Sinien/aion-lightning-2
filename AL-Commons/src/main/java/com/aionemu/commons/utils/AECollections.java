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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * @author NB4L1
 */
@SuppressWarnings("unchecked")
public class AECollections
{
	private static final Object[]	EMPTY_ARRAY	= new Object[0];

	private static final class EmptyListIterator implements ListIterator<Object>
	{
		private static final ListIterator<Object>	INSTANCE	= new EmptyListIterator();

		@Override
		public boolean hasNext()
		{
			return false;
		}

		@Override
		public Object next()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasPrevious()
		{
			return false;
		}

		@Override
		public Object previous()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public int nextIndex()
		{
			return 0;
		}

		@Override
		public int previousIndex()
		{
			return -1;
		}

		@Override
		public void add(Object obj)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(Object obj)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	private static class EmptyCollection implements Collection<Object>
	{
		private static final Collection<Object>	INSTANCE	= new EmptyCollection();

		@Override
		public boolean add(Object e)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends Object> c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear()
		{
		}

		@Override
		public boolean contains(Object o)
		{
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> c)
		{
			return false;
		}

		@Override
		public boolean isEmpty()
		{
			return true;
		}

		@Override
		public Iterator<Object> iterator()
		{
			return emptyListIterator();
		}

		@Override
		public boolean remove(Object o)
		{
			return false;
		}

		@Override
		public boolean removeAll(Collection<?> c)
		{
			return false;
		}

		@Override
		public boolean retainAll(Collection<?> c)
		{
			return false;
		}

		@Override
		public int size()
		{
			return 0;
		}

		@Override
		public Object[] toArray()
		{
			return EMPTY_ARRAY;
		}

		@Override
		public <T> T[] toArray(T[] a)
		{
			if(a.length != 0)
				a = (T[]) Array.newInstance(a.getClass().getComponentType(), 0);

			return a;
		}

		@Override
		public final String toString()
		{
			return "[]";
		}
	}

	private static final class EmptySet extends EmptyCollection implements Set<Object>
	{
		private static final Set<Object>	INSTANCE	= new EmptySet();
	}

	private static final class EmptyMap implements Map<Object, Object>
	{
		private static final Map<Object, Object>	INSTANCE	= new EmptyMap();

		@Override
		public void clear()
		{
		}

		@Override
		public boolean containsKey(Object key)
		{
			return false;
		}

		@Override
		public boolean containsValue(Object value)
		{
			return false;
		}

		@Override
		public Set<Map.Entry<Object, Object>> entrySet()
		{
			return emptySet();
		}

		@Override
		public Object get(Object key)
		{
			return null;
		}

		@Override
		public boolean isEmpty()
		{
			return true;
		}

		@Override
		public Set<Object> keySet()
		{
			return emptySet();
		}

		@Override
		public Object put(Object key, Object value)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void putAll(Map<? extends Object, ? extends Object> m)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Object remove(Object key)
		{
			return null;
		}

		@Override
		public int size()
		{
			return 0;
		}

		@Override
		public Collection<Object> values()
		{
			return emptyCollection();
		}

		@Override
		public String toString()
		{
			return "{}";
		}
	}

	private static <T> ListIterator<T> emptyListIterator()
	{
		return (ListIterator<T>) EmptyListIterator.INSTANCE;
	}

	private static <T> Collection<T> emptyCollection()
	{
		return (Collection<T>) EmptyCollection.INSTANCE;
	}

	public static <T> Set<T> emptySet()
	{
		return (Set<T>) EmptySet.INSTANCE;
	}

	public static <K, V> Map<K, V> emptyMap()
	{
		return (Map<K, V>) EmptyMap.INSTANCE;
	}
}
