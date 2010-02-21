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

import de.kapsi.net.daap.chunks.SByteChunk;

/**
 * The relative loudness of the Song to the main volume
 * adjuster. You can increase or decrease the loundness
 * by +/- 100%.
 *
 * @author  Roger Kapsi
 */
public class SongRelativeVolume extends SByteChunk {
    
    /** Decrease the volume by 100% */
    public static final int MIN_VALUE = -100;
    
    /** Do not increase or decrease the sound volume */
    public static final int NONE = 0;
    
    /** Increase the volume by 100% */
    public static final int MAX_VALUE = 100;
  
    public SongRelativeVolume() {
        this(0);
    }
    
    /**
     * @param <tt>volume</tt> the relative volume
     */
    public SongRelativeVolume(int volume) {
        super("asrv", "daap.songrelativevolume", volume);
    }
}
