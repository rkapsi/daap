
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.ByteChunk;

/**
 * Unknown purpose.
 */
public class BasePlaylist extends ByteChunk {
    
    public BasePlaylist() {
        this(0);
    }
    
    public BasePlaylist(int playlist) {
        super("abpl", "daap.baseplaylist", playlist);
    }
}
