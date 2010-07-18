/*
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004-2010 Roger Kapsi
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

package org.ardverk.daap;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * An abstract base class for DaapAudioResponses aka Streams (derived classes
 * implement the actual streaming).
 * 
 * @author Roger Kapsi
 */
public abstract class DaapAudioResponse implements DaapResponse {

    protected final DaapRequest request;
    protected final Song song;
    protected final FileInputStream in;
    protected final long end;
    protected final byte[] header;

    protected long pos;

    /** Creates a new instance of DaapAudioResponse */
    public DaapAudioResponse(DaapRequest request, Song song,
            FileInputStream in, long pos, long end) {
        this.request = request;
        this.song = song;
        this.in = in;
        this.pos = pos;
        this.end = end;

        header = DaapHeaderConstructor.createAudioHeader(request, pos, end,
                song.getSize());
    }

    @Override
    public String toString() {
        return (new String(header));
    }

    protected void close() throws IOException {
        pos = end;

        if (in != null) {
            in.close();
        }
    }
}