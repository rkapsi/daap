
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.IntChunk;

/**
 * The number of songs, databases or whatever are retuned with
 * the current response. This chunk usually appears together with
 * SpecifiedTotalCount.
 *
 * @see SpecifiedTotalCount
 */
public class ReturnedCount extends IntChunk {
    
    public ReturnedCount() {
        this(0);
    }
    
    public ReturnedCount(int count) {
        super("mrco", "dmap.returnedcount", count);
    }
}
