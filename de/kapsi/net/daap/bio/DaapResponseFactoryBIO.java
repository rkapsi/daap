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
import de.kapsi.net.daap.DaapRequest;
import de.kapsi.net.daap.DaapResponseFactory;

/**
 *
 * @author  roger
 */
class DaapResponseFactoryBIO implements DaapResponseFactory {
    
    /** Creates a new instance of DaapResponseFactoryBIO */
    DaapResponseFactoryBIO() {
    }
    
    public DaapResponse createAudioResponse(DaapRequest request, Song song, FileInputStream in, int pos, int end) throws IOException {
        return new DaapAudioResponseBIO(request, song, in, pos, end);
    }
    
    public DaapResponse createAuthResponse(DaapRequest request) {
        return new DaapAuthResponseBIO(request);
    }
    
    public DaapResponse createChunkResponse(DaapRequest request, byte[] data) {
        return new DaapChunkResponseBIO(request, data);
    }
}
