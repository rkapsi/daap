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
import de.kapsi.net.daap.DaapConnection;
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
    public DaapChunkResponseBIO(DaapConnection connection, byte[] data) {
        super(connection, data);
        
        out = ((DaapConnectionBIO)connection).getOutputStream();
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
