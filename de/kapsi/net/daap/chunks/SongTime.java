
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.IntChunk;

/**
 * The length of this song in milliseconds. You can maybe
 * map this to an IDv2/IDv3 Tag.
 */
public class SongTime extends IntChunk {
    
    /**
     * Creates a new SongTime with 0 length.
     * You can change this value with {@see #setValue(int)}.
     */
    public SongTime() {
        this(0);
    }
    
    /**
     * Creates a new SongTime with the assigned time.
     * You can change this value with {@see #setValue(int)}.
     * @param <tt>time</tt> the length of this song in milliseconds.
     */
    public SongTime(int time) {
        super("astm", "daap.songtime", time);
    }
}
