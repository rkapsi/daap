/*
 * DaapAuthResponse.java
 *
 * Created on April 5, 2004, 8:43 PM
 */

package de.kapsi.net.daap;

/**
 *
 * @author  roger
 */
public abstract class DaapAuthResponse implements DaapResponse {
    
    protected final DaapConnection connection;
    protected final byte[] header;
    
    /** Creates a new instance of DaapAuthResponse */
    public DaapAuthResponse(DaapConnection connection) {
        this.connection = connection;
        header = DaapHeaderConstructor.createAuthHeader(connection);
    }
    
    public String toString() {
        return (new String(header));
    }
}
