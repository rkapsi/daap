
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.ByteChunk;

/**
 * Unknown purpose
 */
public class SongRelativeVolume extends ByteChunk {
    
    /**
     *
     */
    public SongRelativeVolume() {
        this(0);
    }
    
    /**
     * @param <tt>volume</tt> the volume
     */
    public SongRelativeVolume(int volume) {
        super("asrv", "daap.songrelativevolume", volume);
    }
}
