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

/**
 *
 * @author  roger
 */
public class DaapChunkResponse implements DaapResponse {
    
    private SocketChannel channel;
    
    private DaapHeader header;
    private ByteBuffer out;
    
    /** Creates a new instance of DaapChunkResponse */
    public DaapChunkResponse(DaapConnection connection, byte[] data) {
                
        channel = ((DaapConnectionImpl)connection).getChannel();
        
        out = ByteBuffer.wrap(data);
        
        header = DaapHeader.createChunkHeader(connection, data.length);
    }
    
    public boolean hasRemainig() {
        if (header != null && header.hasRemaining())
            return true;
        else return (out != null && out.hasRemaining());
    }
    
    
    /**
     *
     * @throws IOException
     * @return
     */    
    public boolean write() throws IOException {
        
        if (header != null && header.hasRemaining()) {
            if (!header.write()) {
                return false;
            } else {
                header = null;
            }
        }
        
        else if (out != null && out.remaining() > 0) {
            channel.write(out);
            if (out.remaining() > 0) {
                return false;
            } else {
                out = null;
            }
        }
        
        return true;
    }
    
    /**
     *
     * @return
     */    
    public String toString() {
        return header.toString();
    }
}
