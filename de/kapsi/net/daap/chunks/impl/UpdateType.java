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

package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.BooleanChunk;

/**
 * This Chunk is used to indicate if a response is
 * either an update or a full response. The first request
 * is always followed by a full response (e.g. the list of
 * all Songs in the Library) and thenceforward it's awlays
 * an update (a diff between client's and server's Library).
 *
 * @author  Roger Kapsi
 */
public class UpdateType extends BooleanChunk {
    
    /**
     * Creates a new UpdateType which is initialized with <code>false</code>.
     * You can change this value with {@see #setValue(boolean)}.
     */
    public UpdateType() {
        this(false);
    }
    
    /**
     * Creates a new UpdateType with the assigned rating.
     * You can change this value with {@see #setValue(boolean)}.
     * @param <code>updatetype</code> either false or true
     */
    public UpdateType(boolean updatetype) {
        super("muty", "dmap.updatetype", updatetype);
    }
}
