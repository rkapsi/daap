
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.StringChunk;

/**
 * The composer of this song. You can maybe map this to an IDv2/IDv3 Tag.
 */
public class SongComposer extends StringChunk {
    
    public SongComposer() {
        this(null);
    }
    
    public SongComposer(String composer) {
        super("ascp", "daap.songcomposer", composer);
    }
}
