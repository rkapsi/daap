/*
 * DaapResponseFactoryImpl.java
 *
 * Created on April 5, 2004, 7:02 PM
 */

package de.kapsi.net.daap.nio;

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
class DaapResponseFactoryNIO implements DaapResponseFactory {
    
    private DaapConnection connection;
    
    /** Creates a new instance of DaapResponseFactoryNIO */
    DaapResponseFactoryNIO(DaapConnection connection) {
        this.connection = connection;
    }
    
    public DaapResponse createAudioResponse(Song song, FileInputStream in, int pos, int end) throws IOException {
        return new DaapAudioResponseNIO(connection, song, in, pos, end);
    }
    
    public DaapResponse createAuthResponse() {
        return new DaapAuthResponseNIO(connection);
    }
    
    public DaapResponse createChunkResponse(byte[] data) {
        return new DaapChunkResponseNIO(connection, data);
    }
}
