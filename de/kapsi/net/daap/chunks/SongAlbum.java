
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.StringChunk;

/**
 * The album of this song. You can maybe map this to an IDv2/IDv3 Tag.
 */
public class SongAlbum extends StringChunk {
    
    public SongAlbum() {
        this(null);
    }
    
    public SongAlbum(String value) {
        super("asal", "daap.songalbum", value);
    }
}
