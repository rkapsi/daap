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

import de.kapsi.net.daap.chunks.IntChunk;

/**
 * The start time of the Song in milliseconds. I.e. you can use it
 * to skip n-milliseconds at the beginning.
 *
 * @author  Roger Kapsi
 */
public class SongStartTime extends IntChunk {
    
    /**
     * Creates a new SongStartTime which starts at the
     * beginning of the song.
     * You can change this value with {@see #setValue(int)}.
     */
    public SongStartTime() {
        this(0);
    }
    
    /**
     * Creates a new SongStartTime at the assigned time.
     * You can change this value with {@see #setValue(int)}.
     * @param <tt>time</tt> the start time of this song in milliseconds.
     */
    public SongStartTime(int time) {
        super("asst", "daap.songstarttime", time);
    }
}
