/* 
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004 Roger Kapsi, info at kapsi dot de
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.kapsi.util;

import java.util.Iterator;

/**
 * An Iterator for Arrays.
 *
 * @author  Roger Kapsi
 */
public class ArrayIterator implements Iterator {
	
    private Object[] array;
    private int i;
    private int index;
    private int length;

    /**
     * Creates a new ArrayIterator for all elements
     * from 0 to array.length
     * 
     * @param array the Array
     */
    public ArrayIterator(Object[] array) {
        this(array, 0, array.length);
    }

    /**
     * Creates a new ArrayIterator for elements from
     * index to index+length
     * 
     * @param array the Array
     * @param index starting index
     * @param length the number of elements begining at index
     */
    public ArrayIterator(Object[] array, int index, int length) {
        this.array = array;
        this.index = index;
        this.length = length;
        this.i = 0;
    }

    /**
     * Returns <code>true</code> if ArrayIterator
     * has more elements 
     */
    public boolean hasNext() {
        return (i < length);
    }

    /**
     * Returns the current element and increments
     * the internal index by 1
     */
    public Object next() {
        Object obj = array[index+i];
        i++;
        return obj;
    }

    /**
     * Throws an UnsupportedOperationException
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
