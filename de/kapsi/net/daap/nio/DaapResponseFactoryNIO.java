/*
 * DaapResponseFactoryImpl.java
 *
 * Created on April 5, 2004, 7:02 PM
 */

package de.kapsi.net.daap.nio;

import java.io.IOException;
import java.io.FileInputStream;

import de.kapsi.net.daap.Song;
import de.kapsi.net.daap.DaapRequest;
import de.kapsi.net.daap.DaapResponse;
import de.kapsi.net.daap.DaapConnection;
import de.kapsi.net.daap.DaapResponseFactory;


/**
 *
 * @author  roger
 */
class DaapResponseFactoryNIO implements DaapResponseFactory {
    
    /** Creates a new instance of DaapResponseFactoryNIO */
    DaapResponseFactoryNIO() {
    }
    
    public DaapResponse createAudioResponse(DaapRequest request, Song song, FileInputStream in, int pos, int end) throws IOException {
        return new DaapAudioResponseNIO(request, song, in, pos, end);
    }
    
    public DaapResponse createAuthResponse(DaapRequest request) {
        return new DaapAuthResponseNIO(request);
    }
    
    public DaapResponse createChunkResponse(DaapRequest request, byte[] data) {
        return new DaapChunkResponseNIO(request, data);
    }
}
