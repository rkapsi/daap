/*
 * DaapChunkResponse.java
 *
 * Created on April 2, 2004, 8:01 PM
 */

package de.kapsi.net.daap.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import de.kapsi.net.daap.DaapUtil;
import de.kapsi.net.daap.DaapResponse;
import de.kapsi.net.daap.chunks.Chunk;
import de.kapsi.net.daap.DaapConnection;
import de.kapsi.net.daap.DaapChunkResponse;

/**
 *
 * @author  roger
 */
public class DaapChunkResponseImpl extends DaapChunkResponse {
    
    private SocketChannel channel;
    
    private ByteBuffer headerBuffer;
    private ByteBuffer dataBuffer;
    
    /** Creates a new instance of DaapChunkResponse */
    public DaapChunkResponseImpl(DaapConnection connection, byte[] data) {
        super(connection, data);
        
        channel = ((DaapConnectionImpl)connection).getChannel();
        
        headerBuffer = ByteBuffer.wrap(header);
        dataBuffer = ByteBuffer.wrap(data);
    }
    
    public boolean hasRemainig() {
        return headerBuffer.hasRemaining() || dataBuffer.hasRemaining();
    }
    
    /**
     *
     * @throws IOException
     * @return
     */    
    public boolean write() throws IOException {
        
        if (headerBuffer.hasRemaining()) {
            channel.write(headerBuffer);
            
            if (headerBuffer.hasRemaining())
                return false;
        
        }
        
        if (dataBuffer.hasRemaining()) {
            channel.write(dataBuffer);
            return !dataBuffer.hasRemaining();
        }
        
        return true;
    }
}
