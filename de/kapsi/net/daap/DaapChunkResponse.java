/*
 * DaapChunkResponse.java
 *
 * Created on April 5, 2004, 8:43 PM
 */

package de.kapsi.net.daap;

/**
 *
 * @author  roger
 */
public abstract class DaapChunkResponse implements DaapResponse {
    
    protected final DaapRequest request;
    protected final byte[] data;
    protected final byte[] header;
    
    /** Creates a new instance of DaapChunkResponse */
    public DaapChunkResponse(DaapRequest request, byte[] data) {
        this.request = request;
        this.data = data;
        
        header = DaapHeaderConstructor.createChunkHeader(request, data.length);
    }
    
    public String toString() {
        return (new String(header));
    }
}
