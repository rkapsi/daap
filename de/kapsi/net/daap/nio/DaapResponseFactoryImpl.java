/*
 * DaapResponseFactoryImpl.java
 *
 * Created on April 5, 2004, 7:02 PM
 */

package de.kapsi.net.daap.nio;

import java.io.IOException;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;

import de.kapsi.net.daap.DaapResponseFactory;
import de.kapsi.net.daap.DaapConnection;
import de.kapsi.net.daap.DaapResponse;

/**
 *
 * @author  roger
 */
public class DaapResponseFactoryImpl implements DaapResponseFactory {
    
    private DaapConnection connection;
    
    /** Creates a new instance of DaapResponseFactoryImpl */
    public DaapResponseFactoryImpl(DaapConnection connection) {
        this.connection = connection;
    }
    
    public DaapResponse createAudioResponse(FileInputStream in, int pos, int end) throws IOException {
        FileChannel channel = in.getChannel();
        return new DaapAudioResponse(connection, channel, pos, end);
    }
    
    public DaapResponse createAuthResponse() {
        return new DaapAuthResponse(connection);
    }
    
    public DaapResponse createChunkResponse(byte[] data) {
        return new DaapChunkResponse(connection, data);
    }
}
