/*
 * DaapAuthResponse.java
 *
 * Created on April 2, 2004, 7:58 PM
 */

package de.kapsi.net.daap.nio;

import java.io.IOException;
import de.kapsi.net.daap.DaapResponse;
import de.kapsi.net.daap.DaapConnection;

/**
 *
 * @author  roger
 */
public class DaapAuthResponse implements DaapResponse {
    
    private DaapHeader header;
    
    /** Creates a new instance of DaapAuthResponse */
    public DaapAuthResponse(DaapConnection connection) {
        header = DaapHeader.createAuthHeader(connection);
    }
    
    
    public boolean hasRemainig() {
        return header.hasRemaining();
    }
    
    
    /**
     *
     * @throws IOException
     * @return
     */    
    public boolean write() throws IOException {
        return header.write();
    }
    
    /**
     *
     * @return
     */    
    public String toString() {
        return header.toString();
    }
}
