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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is an implementation of a date chunk.
 * The date is an integer int seconds since 1.1.1970.
 *
 * @author  Roger Kapsi
 */
public abstract class DateChunk extends AbstractChunk {
    
    private static final Logger LOG = LoggerFactory.getLogger(DateChunk.class);
    
    public static final long MIN_VALUE = 0l;
    public static final long MAX_VALUE = 0xFFFFFFFFl;
    
    protected int date;
    
    public DateChunk(int type, String name, long value) {
        super(type, name);
        setValue(value);
    }
    
    public DateChunk(String type, String name, long date) {
        super(type, name);
        setValue(date);
    }
    
    public long getValue() {
        return date & MAX_VALUE;
    }
    
    public void setValue(long date) {
        this.date = (int)checkDateRange(date);
    }
    
    /**
     * Checks if #MIN_VALUE <= value <= #MAX_VALUE and if 
     * not an IllegalArgumentException is thrown.
     */
    public static long checkDateRange(long value) 
            throws IllegalArgumentException {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Value is outside of Date range: " + value);
            }
        }
        return value;
    }
    
    /**
     * Returns {@see #DATE_TYPE}
     */
    public int getType() {
        return Chunk.DATE_TYPE;
    }
    
    public String toString(int indent) {
        return indent(indent) + name + "(" + getContentCodeString() + "; date)="+getValue();
    }
}
