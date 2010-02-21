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

import org.ardverk.daap.chunks.StringChunk;

/**
 * The format of the Song. Note: the provided list of supported
 * format fields is incomplete.
 *
 * @author  Roger Kapsi
 */
public class SongFormat extends StringChunk {
    
    public static final String UNKNOWN = null;
    
    /**
     * Audio Interchange File Format (AIFF). This format is very popular
     * upon Apple platforms, and is widely used in professional
     * programs that process digital audio waveforms.
     */
    public static final String AIFF = "aiff";
    
    /**
     * MPEG4 Advanced Audio Coding (AAC).
     */
    public static final String M4A = "m4a";
    
    /**
     * MPEG Audio Layer 3 (MP3)
     */
    public static final String MP3 = "mp3";
    
    /**
     * Wave file (WAV)
     */
    public static final String WAV = "wav";
    
    /**
     * Playlist
     */
    public static final String PLS = "pls";
    
    /**
     * Creates a new SongFormat where format is not set.
     * You can change this value with {@see #setValue(String)}.
     */
    public SongFormat() {
        this(UNKNOWN);
    }
    
    /**
     * Creates a new SongFormat with the assigned format.
     * You can change this value with {@see #setValue(String)}.
     * @param <tt>format</tt> the format of this song or <tt>null</tt>
     * if no format is set.
     */
    public SongFormat(String format) {
        super("asfm", "daap.songformat", format);
    }
}
