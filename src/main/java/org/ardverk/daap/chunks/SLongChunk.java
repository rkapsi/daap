/*
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004-2010 Roger Kapsi
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
 * A signed long
 */
public abstract class SLongChunk extends AbstractChunk implements LongChunk {

    public static final long MIN_VALUE = Long.MIN_VALUE;
    public static final long MAX_VALUE = Long.MAX_VALUE;

    protected long value = 0;

    public SLongChunk(int type, String name, long value) {
        super(type, name);
        setValue(value);
    }

    public SLongChunk(String type, String name, long value) {
        super(type, name);
        setValue(value);
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    /**
     * Returns {@see #LONG_TYPE}
     */
    public int getType() {
        return Chunk.LONG_TYPE;
    }

    public String toString(int indent) {
        return indent(indent) + name + "(" + getContentCodeString()
                + "; long)=" + getValue();
    }
}