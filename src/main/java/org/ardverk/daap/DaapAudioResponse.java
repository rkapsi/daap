/*
 * Digital Audio Access Protocol (DAAP) Library
 * Copyright (C) 2004-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
