
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.StringChunk;

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
