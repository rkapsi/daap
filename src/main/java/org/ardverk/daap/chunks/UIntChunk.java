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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
* An unsigned int
*/
public abstract class UIntChunk extends AbstractChunk implements IntChunk {

private static final Logger LOG = LoggerFactory.getLogger(UIntChunk.class);

public static final long MIN_VALUE = 0l;
public static final long MAX_VALUE = 0xFFFFFFFFl;

protected int value = 0;

public UIntChunk(int type, String name, long value) {
super(type, name);
setValue(value);
}

public UIntChunk(String type, String name, long value) {
super(type, name);
setValue(value);
}

public void setValue(int value) {
this.value = value;
}

public void setValue(long value) {
setValue((int)checkUIntRange(value));
}

public int getValue() {
return value;
}

public long getUnsignedValue() {
return getValue() & MAX_VALUE;
}

/**
* Checks if #MIN_VALUE <= value <= #MAX_VALUE and if
* not an IllegalArgumentException is thrown.
*/
public static long checkUIntRange(long value)
throws IllegalArgumentException {
if (value < MIN_VALUE || value > MAX_VALUE) {
if (LOG.isErrorEnabled()) {
LOG.error("Value is outside of unsigned int range: " + value);
}
}
return value;
}

/**
* Returns {@see #U_INT_TYPE}
*/
public int getType() {
return Chunk.U_INT_TYPE;
}

public String toString(int indent) {
return indent(indent) + name + "(" + getContentCodeString() + "; uint)=" + getUnsignedValue();
}
}