
package de.kapsi.net.daap.nio;

import java.io.IOException;

/**
 * 
 */
public interface DaapResponse {
    
    /**
     * Returns <tt>true</tt> when the write() operation
     * is complete and <tt>false</tt> when some bytes
     * were left which shall be written at the next
     * iteration...
     * @throws IOException
     * @return <tt>true</tt> if write() operation is complete
     */
    public boolean write() throws IOException;
}
