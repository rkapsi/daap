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

package org.ardverk.daap.chunks;

/**
 * A signed int
 */
public abstract class SIntChunk extends AbstractChunk implements IntChunk {
    
    public static final int MIN_VALUE = Integer.MIN_VALUE;
    public static final int MAX_VALUE = Integer.MAX_VALUE;
    
    protected int value = 0;
    
    public SIntChunk(int type, String name, int value) {
        super(type, name);
        setValue(value);
    }
    
    public SIntChunk(String type, String name, int value) {
        super(type, name);
        setValue(value);
    }
    
    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Returns {@see #INT_TYPE}
     */
    public int getType() {
        return Chunk.INT_TYPE;
    }

    public String toString(int indent) {
        return indent(indent) + name + "(" + getContentCodeString() + "; int)=" + getValue();
    }
}

