
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.DateChunk;

/**
 * The date when this song was modified. Date is the difference between
 * the current time and midnight, January 1, 1970 UTC in <u>seconds</u>!
 *
 * <p>int date = (int)(System.currentTimeMillis()/1000);</p>
 */
public class SongDateModified extends DateChunk {
    
    public SongDateModified() {
        this(0);
    }
    
    public SongDateModified(int date) {
        super("asdm", "daap.songdatemodified", date);
    }
}
