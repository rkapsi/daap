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
 * Enables or disables this song. Default is enabled. iTunes shows this
 * as the small checkbox next to the song name.
 */
public class SongDisabled extends BooleanChunk {
    
    /**
     * Creates a new SongDisabled where song is enabled.
     * You can change this value with {@see #setValue(boolean)}.
     */
    public SongDisabled() {
        this(false);
    }
    
    /**
     * Creates a new SongDisabled with the assigned value.
     * You can change this value with {@see #setValue(boolean)}.
     * @param <tt>disabled</tt> enables or disables this song.
     */
    public SongDisabled(boolean disabled) {
        super("asdb", "daap.songdisabled", disabled);
    }
}
