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
    
    protected final DaapConnection connection;
    protected final Song song;
    protected final FileInputStream in;
    protected final int end;
    protected final byte[] header;
    
    protected int pos;
    
    /** Creates a new instance of DaapAudioResponse */
    public DaapAudioResponse(DaapConnection connection, Song song, FileInputStream in, int pos, int end) throws IOException {
        this.connection = connection;
        this.song = song;
        this.in = in;
        this.pos = pos;
        this.end = end;
        
        header = DaapHeaderConstructor.createAudioHeader(connection, song.getSize());
    }
    
    public String toString() {
        return (new String(header));
    }
}
