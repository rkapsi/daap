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

package de.kapsi.net.daap.chunks;

import java.io.OutputStream;
import java.io.IOException;

/**
 * A Chuck is a tagged value (key/value pair). Chunks can either 
 * contain other Chunks or data of various types.
 * 
 * @author  Roger Kapsi
 */
public interface Chunk {
    
    /** Type for byte Chunks */
    public static final int BYTE_TYPE       = 1;
    
    /** 
     * Type for boolean Chunks. NOTE: this type is actually not
     * defined in DAAP and it is my own construct for Java. If you
     * do an DAAP protocol analysis you will see that boolean
     * values are bytes where <code>0 == false</code> and 
     * <code>!0 == true</code>. So a boolean type is actually a
     * {@see #BYTE_TYPE}.
     */
    public static final int BOOLEAN_TYPE    = BYTE_TYPE;
    
    /** Type for short Chunks */
    public static final int SHORT_TYPE      = 3;
    
    /** Type for int Chunks */
    public static final int INT_TYPE        = 5;
    
    /** Type for long Chunks */
    public static final int LONG_TYPE       = 7;
    
    /** Type for String Chunks (encoded as UTF-8) */
    public static final int STRING_TYPE     = 9;
    
    /** Type for Date Chunks (Time in <u>seconds</u> since 1970) */
    public static final int DATE_TYPE       = 10;
    
    /** 
     * Type for Version Chunks (an int value split up into major, minor 
     * and patch level)
     */
    public static final int VERSION_TYPE    = 11;
    
    /** Type for Container Chunks. Chunks that contain other Chunks */
    public static final int CONTAINER_TYPE  = 12;
    
    /**
     * Returns the size of this Chunk. The size is defined as
     * header + {@see #getLength()} in bytes.
     */
    public int getSize();
    
    /**
     * Returns the length of this Chunk's payload in bytes.
     */
    public int getLength();
    
    /**
     * Returns the type of this Chunk. For example {@see #BOOLEAN_TYPE}.
     */
    public int getType();
    
    /**
     * Writes the serialized form of this Chunk to <code>out</code>
     */
    public void serialize(OutputStream out) throws IOException;
}
