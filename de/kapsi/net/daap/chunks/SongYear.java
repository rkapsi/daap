
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ShortChunk;

/**
 * The year this song was released. You can maybe map this to an IDv2/IDv3 Tag.
 */
public class SongYear extends ShortChunk {
    
    /**
     * Creates a new SongYear and initializes it with 0
     * You can change this value with {@see #setValue(int)}.
     */
    public SongYear() {
        this(0);
    }
    
    /**
     * Creates a new SongYear and initializes it with
     * the assigned year.
     * You can change this value with {@see #setValue(int)}.
     * @param <tt>year</tt> the year
     */
    public SongYear(int year) {
        super("asyr", "daap.songyear", year);
    }
}
