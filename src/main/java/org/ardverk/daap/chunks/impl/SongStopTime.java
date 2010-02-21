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
 * The stop time of the Song in milliseconds. I.e. you can use it
 * to stop playing this song n-milliseconds before end.
 *
 * @author  Roger Kapsi
 */
public class SongStopTime extends UIntChunk {
    
    /**
     * Creates a new SongStopTime where stop time is not set.
     * You can change this value with {@see #setValue(int)}.
     */
    public SongStopTime() {
        this(0);
    }
    
    /**
     * Creates a new SongStopTime at the assigned time. Use 0
     * to disable this property.
     * You can change this value with {@see #setValue(int)}.
     * @param <tt>time</tt> the stop time of this song in milliseconds.
     */
    public SongStopTime(long time) {
        super("assp", "daap.songstoptime", time);
    }
}
