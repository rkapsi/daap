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

package de.kapsi.net.daap;

import java.io.IOException;
import java.io.FileInputStream;

/**
 * An abstract base class for DaapAudioResponses aka Streams (derived 
 * classes implement the actual streaming).
 *
 * @author  Roger Kapsi
 */
public abstract class DaapAudioResponse implements DaapResponse {
    
    protected final DaapRequest request;
    protected final Song song;
    protected final FileInputStream in;
    protected final int end;
    protected final byte[] header;
    
    protected int pos;
    
    /** Creates a new instance of DaapAudioResponse */
    public DaapAudioResponse(DaapRequest request, Song song, FileInputStream in, int pos, int end) throws IOException {
        this.request = request;
        this.song = song;
        this.in = in;
        this.pos = pos;
        this.end = end;
        
        header = DaapHeaderConstructor.createAudioHeader(request, pos, end, song.getSize());
    }
    
    public String toString() {
        return (new String(header));
    }
}
