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

package org.ardverk.daap.chunks.impl;

import org.ardverk.daap.chunks.UIntChunk;

/**
 * Seems to be the equivalent to HTTP/1.1 200 OK but it's never
 * changing even if an error occurs.
 *
 * @author  Roger Kapsi
 */
public class Status extends UIntChunk {
    
    /**
     * The default status
     */
    public static final int STATUS_200 = 200;
    
    public Status() {
        this(STATUS_200);
    }
    
    public Status(long status) {
        super("mstt", "dmap.status", status);
    }
}
