
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.StringChunk;

/**
 * The comment of this song. You can maybe map this to an IDv2/IDv3 Tag.
 */
public class SongComment extends StringChunk {
    
    public SongComment() {
        this(null);
    }
    
    public SongComment(String comment) {
        super("ascm", "daap.songcomment", comment);
    }
}
