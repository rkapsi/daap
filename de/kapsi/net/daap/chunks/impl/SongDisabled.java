
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.BooleanChunk;

/**
 * Enables or disables this song. Default is enabled. iTunes shows this
 * as the small checkbox next to the song name.
 */
public class SongDisabled extends BooleanChunk {
    
    /**
     * Creates a new SongDisabled where song is enabled.
     * You can change this value with {@see #setValue(boolean)}.
     */
    public SongDisabled() {
        this(false);
    }
    
    /**
     * Creates a new SongDisabled with the assigned value.
     * You can change this value with {@see #setValue(boolean)}.
     * @param <tt>disabled</tt> enables or disables this song.
     */
    public SongDisabled(boolean disabled) {
        super("asdb", "daap.songdisabled", disabled);
    }
}
