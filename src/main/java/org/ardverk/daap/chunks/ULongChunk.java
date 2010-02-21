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

import java.math.BigInteger;

/**
 * An unsigned long
 */
public abstract class ULongChunk extends AbstractChunk implements LongChunk {

    public static final String MIN_VALUE = "0";
    public static final String MAX_VALUE = "18446744073709551615";

    protected long value = 0;

    public ULongChunk(int type, String name, long value) {
        super(type, name);
        setValue(value);
    }

    public ULongChunk(String type, String name, long value) {
        super(type, name);
        setValue(value);
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public BigInteger getUnsignedValue() {
        long l = getValue();
        byte[] b = new byte[8 + 1];

        b[0] = 0;
        b[1] = (byte) ((l >> 56l) & 0xFF);
        b[2] = (byte) ((l >> 48l) & 0xFF);
        b[3] = (byte) ((l >> 40l) & 0xFF);
        b[4] = (byte) ((l >> 32l) & 0xFF);
        b[5] = (byte) ((l >> 24l) & 0xFF);
        b[6] = (byte) ((l >> 16l) & 0xFF);
        b[7] = (byte) ((l >> 8l) & 0xFF);
        b[8] = (byte) ((l) & 0xFF);

        return new BigInteger(b);
    }

    /**
     * Returns {@see #U_LONG_TYPE}
     */
    public int getType() {
        return Chunk.U_LONG_TYPE;
    }

    public String toString(int indent) {
        return indent(indent) + name + "(" + getContentCodeString()
                + "; ulong)=" + getUnsignedValue();
    }
}