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

import de.kapsi.net.daap.chunks.ShortChunk;

/**
 * The total number of disks.
 *
 * @author  Roger Kapsi
 */
public class SongDiscCount extends ShortChunk {
    
    /**
     * Creates a new SongDiscCount where count is 0.
     * You can change this value with {@see #setValue(int)}.
     */
    public SongDiscCount() {
        this(0);
    }
    
    /**
     * Creates a new SongDiscNumber with the assigned disc.
     * You can change this value with {@see #setValue(int)}.
     * @param <tt>count</tt> the count of discs this album has where
     * this song belongs to.
     */
    public SongDiscCount(int count) {
        super("asdc", "daap.songdisccount", count);
    }
}
