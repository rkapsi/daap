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
    
    protected final DaapRequest request;
    protected final byte[] header;
    
    /** Creates a new instance of DaapAuthResponse */
    public DaapAuthResponse(DaapRequest request) {
        this.request = request;
        header = DaapHeaderConstructor.createAuthHeader(request);
    }
    
    public String toString() {
        return (new String(header));
    }
}
