
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.ShortChunk;

/**
 * The number of discs this album has where this song belongs to.
 * You can maybe map this to an IDv2/IDv3 Tag.
 */
public class SongDiscCount extends ShortChunk {
    
    /**
     * Creates a new SongDiscCount where count is 0.
     * You can change this value with {@see #setValue(int)}.
     */
    public SongDiscCount() {
        this(0);
    }
    
    /**
     * Creates a new SongDiscNumber with the assigned disc.
     * You can change this value with {@see #setValue(int)}.
     * @param <tt>count</tt> the count of discs this album has where
     * this song belongs to.
     */
    public SongDiscCount(int count) {
        super("asdc", "daap.songdisccount", count);
    }
}
