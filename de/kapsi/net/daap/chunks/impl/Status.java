
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.IntChunk;

/**
 * Seems to be the equivalent to HTTP/1.1 200 OK but it's never
 * changing even if an error occurs.
 */
public class Status extends IntChunk {
    
    /**
     * The default status
     */
    public static final int STATUS_200 = 200;
    
    public Status() {
        this(STATUS_200);
    }
    
    public Status(int status) {
        super("mstt", "dmap.status", status);
    }
}
