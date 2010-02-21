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

import java.io.UnsupportedEncodingException;

import org.ardverk.daap.DaapUtil;

/**
* This class implements a String chunk. DAAP Strings are
* encoded in UTF-8.
*
* @author  Roger Kapsi
*/
public abstract class StringChunk extends AbstractChunk {

protected String value;

public StringChunk(int type, String name, String value) {
super(type, name);
setValue(value);
}

public StringChunk(String type, String name, String value) {
super(type, name);
setValue(value);
}

public String getValue() {
return value;
}

public void setValue(String value) {
this.value = value;
}

public byte[] getBytes() {
String value = this.value;

if (value == null || value.length() == 0) {
return new byte[0];
} else {
try {
return value.getBytes(DaapUtil.UTF_8);
} catch (UnsupportedEncodingException err) {
// Should never happen
throw new RuntimeException(err);
}
}
}

/**
* Returns {@see #STRING_TYPE}
*/
public int getType() {
return Chunk.STRING_TYPE;
}

public String toString(int indent) {
return indent(indent) + name + "(" + getContentCodeString() + "; string)=" + getValue();
}
}