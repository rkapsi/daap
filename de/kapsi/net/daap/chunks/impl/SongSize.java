
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.IntChunk;

/**
 * The size of this song in bytes.
 */
public class SongSize extends IntChunk {
    
    /**
     * Creates a new SongSize with 0-length
     * You can change this value with {@see #setValue(int)}.
     */
    public SongSize() {
        this(0);
    }
    
    /**
     * Creates a new SongSize with the assigned size.
     * You can change this value with {@see #setValue(int)}.
     * @param <tt>size</tt> the size of this song in bytes.
     */
    public SongSize(int size) {
        super("assz", "daap.songsize", size);
    }
}
