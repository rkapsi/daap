/*
 * DaapResponse.java
 *
 * Created on April 5, 2004, 6:39 PM
 */

package de.kapsi.net.daap;

import java.io.IOException;

/**
 * An interface for either NIO or classic I/O based
 * DaapRespones
 *
 * @author  roger
 */
public interface DaapResponse {
    
    /**
     * Returns <tt>true</tt> if some bytes were leftover
     * or <tt>false</tt> all bytes were written.
     */
    public boolean hasRemainig();
    
    /**
     * Returns <tt>true</tt> when the write() operation
     * is complete and <tt>false</tt> when some bytes
     * were left which shall be written at the next
     * iteration (NIO view, classic I/O DaapRespones will
     * always return <tt>true</tt>).
     *
     * @throws IOException
     * @return <tt>true</tt> if write() operation is complete
     */
    public boolean write() throws IOException;
}
