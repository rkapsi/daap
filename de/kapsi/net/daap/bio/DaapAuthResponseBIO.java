/*
 * DaapAuthResponseImpl.java
 *
 * Created on April 5, 2004, 9:45 PM
 */

package de.kapsi.net.daap.bio;

import java.io.IOException;
import java.io.OutputStream;

import de.kapsi.net.daap.DaapResponse;
import de.kapsi.net.daap.DaapRequest;
import de.kapsi.net.daap.DaapAuthResponse;

/**
 *
 * @author  roger
 */
public class DaapAuthResponseBIO extends DaapAuthResponse {
    
    private boolean headerWritten = false;
    private OutputStream out;
    
    /** Creates a new instance of DaapAuthResponse */
    public DaapAuthResponseBIO(DaapRequest request) {
        super(request);
        
        DaapConnectionBIO connection = (DaapConnectionBIO)request.getConnection();
        out = connection.getOutputStream();
    }
    
    
    public boolean hasRemainig() {
        return !headerWritten;
    }
    
    /**
     *
     * @throws IOException
     * @return
     */    
    public boolean write() throws IOException {
        
        if (!headerWritten) {
            out.write(header, 0, header.length);
            out.flush();
            
            headerWritten = true;
        }
        
        return true;
    }
}
