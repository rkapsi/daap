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

package org.ardverk.daap.chunks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An unsigned byte
 */
public abstract class UByteChunk extends AbstractChunk implements ByteChunk {
    
    private static final Logger LOG = LoggerFactory.getLogger(UByteChunk.class);
    
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 0xFF;
    
    protected int value = 0;
    
    public UByteChunk(int type, String name, int value) {
        super(type, name);
        setValue(value);
    }
    
    public UByteChunk(String type, String name, int value) {
        super(type, name);
        setValue(value);
    }

    public void setValue(int value) {
        this.value = checkUByteRange(value);
    }
    
    public int getValue() {
        return value;
    }
    
    /**
     * Checks if #MIN_VALUE <= value <= #MAX_VALUE and if 
     * not an IllegalArgumentException is thrown.
     */
    public static int checkUByteRange(int value) 
            throws IllegalArgumentException {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Value is outside of unsigned byte range: " + value);
            }
        }
        return value;
    }
    
    /**
     * Returns {@see #U_BYTE_TYPE}
     */
    public int getType() {
        return Chunk.U_BYTE_TYPE;
    }
    
    public String toString(int indent) {
        return indent(indent) + name + "(" + getContentCodeString() + "; ubyte)=" + getValue();
    }
}
