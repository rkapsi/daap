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
    private int from;
    private int to;

    public ArrayIterator(Object[] array) {
        this(array, 0, array.length);
    }

    public ArrayIterator(Object[] array, int from, int to) {
        this.array = array;
        this.from = from;
        this.to = to;
    }

    public boolean hasNext() {
        return (from < to);
    }

    public Object next() {
        return array[from++];
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
