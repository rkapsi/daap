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

public interface Chunk {
    
    public static final int BYTE_TYPE       = 1;
    
    // That's correct. A boolean is a byte! 0 = false, !0 = true
    // (i.e. everything else except 0). As we're on the server side
    // and do not have to deal with parsing etc. it's not important
    // for us but keep it in mind.
    public static final int BOOLEAN_TYPE    = BYTE_TYPE;
    
    public static final int SHORT_TYPE      = 3;
    public static final int INT_TYPE        = 5;
    public static final int LONG_TYPE       = 7;
    public static final int STRING_TYPE     = 9;
    public static final int DATE_TYPE       = 10;
    public static final int VERSION_TYPE    = 11;
    public static final int CONTAINER_TYPE  = 12;
    
    /**
     * Returns the total size (header+payload) of a chunk
     * in bytes.
     */
    public int getSize();
    
    /**
     * Returns the payload of a chunk in bytes.
     */
    public int getLength();
    
    /**
     * Returns the type of a chunk (BOOLEAN_TYPE etc).
     */
    public int getType();
    
    /**
     * Writes 'this' to 'out'
     */
    public void serialize(OutputStream out) throws IOException;
}
