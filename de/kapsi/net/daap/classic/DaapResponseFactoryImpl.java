/*
 * DaapResponseFactoryImpl.java
 *
 * Created on April 5, 2004, 8:14 PM
 */

package de.kapsi.net.daap.classic;

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
class DaapResponseFactoryImpl implements DaapResponseFactory {
    
    private DaapConnection connection;
    
    /** Creates a new instance of DaapResponseFactoryImpl */
    DaapResponseFactoryImpl(DaapConnection connection) {
        this.connection = connection;
    }
    
    public DaapResponse createAudioResponse(Song song, FileInputStream in, int pos, int end) throws IOException {
        return new DaapAudioResponseImpl(connection, song, in, pos, end);
    }
    
    public DaapResponse createAuthResponse() {
        return new DaapAuthResponseImpl(connection);
    }
    
    public DaapResponse createChunkResponse(byte[] data) {
        return new DaapChunkResponseImpl(connection, data);
    }
}
