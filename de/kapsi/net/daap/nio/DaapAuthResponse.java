/*
 * DaapAuthResponse.java
 *
 * Created on April 2, 2004, 7:58 PM
 */

package de.kapsi.net.daap.nio;

import java.io.IOException;

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
