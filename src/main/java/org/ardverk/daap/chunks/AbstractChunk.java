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

import org.ardverk.daap.DaapUtil;

/**
* An abstract base class for Chunks.
*
* @author  Roger Kapsi
*/
public abstract class AbstractChunk implements Chunk {

protected final int contentCode;
protected final String name;

/**
*
*/
protected AbstractChunk(String contentCode, String name) {
if (contentCode.length() != 4) {
throw new IllegalArgumentException("Content Code must be 4 chars");
}

this.contentCode = DaapUtil.toContentCodeNumber(contentCode);
this.name = name;
}

/**
*
*/
protected AbstractChunk(int contentCode, String name) {
this.contentCode = contentCode;
this.name = name;
}

public int getContentCode() {
return contentCode;
}

/**
* Returns the 4 charecter content code of this Chunk as String
*/
public String getContentCodeString() {
return DaapUtil.toContentCodeString(contentCode);
}

/**
* Returns the name of this Chunk
*/
public String getName() {
return name;
}

/**
* Returns the type of this Chunk.
*/
public abstract int getType();

public String toString() {
return toString(0);
}

public String toString(int indent) {
return indent(indent) + name + "('" + getContentCodeString() + "')";
}

protected static String indent(int indent) {
StringBuffer buffer = new StringBuffer(indent);
for(int i = 0; i < indent; i++) {
buffer.append(' ');
}
return buffer.toString();
}
}