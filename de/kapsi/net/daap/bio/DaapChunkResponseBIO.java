/*
 * DaapChunkResponse.java
 *
 * Created on April 5, 2004, 9:37 PM
 */

package de.kapsi.net.daap.bio;

import java.io.IOException;
import java.io.OutputStream;

import de.kapsi.net.daap.DaapUtil;
import de.kapsi.net.daap.DaapResponse;
import de.kapsi.net.daap.chunks.Chunk;
import de.kapsi.net.daap.DaapRequest;
import de.kapsi.net.daap.DaapChunkResponse;

/**
 *
 * @author  roger
 */
public class DaapChunkResponseBIO extends DaapChunkResponse {
    
    private boolean headerWritten = false;
    private boolean dataWritten = false;
    
    private OutputStream out;
    
    /** Creates a new instance of DaapChunkResponse */
    public DaapChunkResponseBIO(DaapRequest request, byte[] data) {
        super(request, data);
        
        DaapConnectionBIO connection = (DaapConnectionBIO)request.getConnection();
        out = connection.getOutputStream();
    }
    
    public boolean hasRemainig() {
        return !(headerWritten && dataWritten);
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
        
        if (!dataWritten) {
            
            out.write(data, 0, data.length);
            out.flush();
        
            dataWritten = true;
        }
       
        return true;
    }
}
