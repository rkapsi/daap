/*
 * DaapAudioResponse.java
 *
 * Created on April 5, 2004, 8:43 PM
 */

package de.kapsi.net.daap;

import java.io.IOException;
import java.io.FileInputStream;

/**
 *
 * @author  roger
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
