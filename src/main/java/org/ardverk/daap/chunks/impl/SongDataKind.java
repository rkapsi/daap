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

import org.ardverk.daap.chunks.UByteChunk;

/**
 * This class describes if a song is either a Radio stream 
 * or DAAP stream. Radio streams have a different icon and
 * and the data is usually streamed from an URL ({@see SongDataUrl}).
 *
 * @author  Roger Kapsi
 */
public class SongDataKind extends UByteChunk {
    
    /** Radio stream */
    public static final int RADIO_STREAM    = 1;
    
    /** DAAP stream (default) */
    public static final int DAAP_STREAM     = 2;
    
    /**
     * Creates a new SongDataKind with DAAP_STREAM
     * as type.
     */
    public SongDataKind() {
        this(DAAP_STREAM);
    }
    
    public SongDataKind(int kind) {
        super("asdk", "daap.songdatakind", kind);
    }
}
