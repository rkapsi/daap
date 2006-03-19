/* 
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2005 Roger Kapsi, info at kapsi dot de
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

package de.kapsi.net.daap.chunks;

/**
 * An unsigned short
 */
public abstract class UShortChunk extends AbstractChunk implements ShortChunk {
    
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 0xFFFF;
    
    protected int value = 0;
    
    public UShortChunk(int type, String name, int value) {
        super(type, name);
        setValue(value);
    }
    
    public UShortChunk(String type, String name, int value) {
        super(type, name);
        setValue(value);
    }

    public void setValue(int value) {
        this.value = checkUShortRange(value);
    }
    
    public int getValue() {
        return value;
    }
    
    /**
     * Checks if #MIN_VALUE <= value <= #MAX_VALUE and if 
     * not an IllegalArgumentException is thrown.
     */
    public static int checkUShortRange(int value)
            throws IllegalArgumentException {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new IllegalArgumentException("Value is outside of unsigned short range: " + value);
        }
        return value;
    }
    
    /**
     * Returns {@see #U_SHORT_TYPE}
     */
    public int getType() {
        return Chunk.U_SHORT_TYPE;
    }
    
    public String toString(int indent) {
        return indent(indent) + name + "(" + getContentCodeString() + "; ushort)=" + getValue();
    }
}

