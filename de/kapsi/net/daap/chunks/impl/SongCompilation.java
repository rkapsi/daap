
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.BooleanChunk;

/**
 *
 */
public class SongCompilation extends BooleanChunk {
    
    public SongCompilation() {
        this(false);
    }
    
    public SongCompilation(boolean comp) {
        super("asco", "daap.songcompilation", comp);
    }
}
