/*
 * DaapResponseFactoryImpl.java
 *
 * Created on April 5, 2004, 8:14 PM
 */

package de.kapsi.net.daap.bio;

import java.io.IOException;
import java.io.FileInputStream;

import de.kapsi.net.daap.Song;
import de.kapsi.net.daap.DaapResponse;
import de.kapsi.net.daap.DaapConnection;
import de.kapsi.net.daap.DaapResponseFactory;

/**
 *
 * @author  roger
 */
class DaapResponseFactoryBIO implements DaapResponseFactory {
    
    private DaapConnection connection;
    
    /** Creates a new instance of DaapResponseFactoryBIO */
    DaapResponseFactoryBIO(DaapConnection connection) {
        this.connection = connection;
    }
    
    public DaapResponse createAudioResponse(Song song, FileInputStream in, int pos, int end) throws IOException {
        return new DaapAudioResponseBIO(connection, song, in, pos, end);
    }
    
    public DaapResponse createAuthResponse() {
        return new DaapAuthResponseBIO(connection);
    }
    
    public DaapResponse createChunkResponse(byte[] data) {
        return new DaapChunkResponseBIO(connection, data);
    }
}
