/*
 * DaapAuthResponse.java
 *
 * Created on April 2, 2004, 7:58 PM
 */

package de.kapsi.net.daap.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import de.kapsi.net.daap.DaapResponse;
import de.kapsi.net.daap.DaapConnection;
import de.kapsi.net.daap.DaapAuthResponse;

/**
 *
 * @author  roger
 */
public class DaapAuthResponseImpl extends DaapAuthResponse {
    
    private ByteBuffer headerBuffer;
    private SocketChannel channel;
    
    /** Creates a new instance of DaapAuthResponse */
    public DaapAuthResponseImpl(DaapConnection connection) {
        super(connection);
        
        channel = ((DaapConnectionImpl)connection).getChannel();
        headerBuffer = ByteBuffer.wrap(header);
    }
    
    
    public boolean hasRemainig() {
        return headerBuffer.hasRemaining();
    }
    
    
    /**
     *
     * @throws IOException
     * @return
     */    
    public boolean write() throws IOException {
        if (headerBuffer.hasRemaining()) {
            channel.write(headerBuffer);
            return !headerBuffer.hasRemaining();
        }
        
        return true;
    }
}
