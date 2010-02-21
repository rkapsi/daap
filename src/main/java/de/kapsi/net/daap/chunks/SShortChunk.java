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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A signed short
 */
public abstract class SShortChunk extends AbstractChunk implements ShortChunk {
    
    private static final Log LOG = LogFactory.getLog(SShortChunk.class);
  
    public static final int MIN_VALUE = Short.MIN_VALUE;
    public static final int MAX_VALUE = Short.MAX_VALUE;
    
    protected int value = 0;
    
    public SShortChunk(int type, String name, int value) {
        super(type, name);
        setValue(value);
    }
    
    public SShortChunk(String type, String name, int value) {
        super(type, name);
        setValue(value);
    }

    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value = checkSShortRange(value);
    }
    
    /**
     * Checks if #MIN_VALUE <= value <= #MAX_VALUE and if 
     * not an IllegalArgumentException is thrown.
     */
    public static int checkSShortRange(int value)
            throws IllegalArgumentException {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Value is outside of signed short range: " + value);
            }
        }
        return value;
    }
    
    /**
     * Returns {@see #SHORT_TYPE}
     */
    public int getType() {
        return Chunk.SHORT_TYPE;
    }
    
    public String toString(int indent) {
        return indent(indent) + name + "(" + getContentCodeString() + "; short)=" + getValue();
    }
}
