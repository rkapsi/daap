
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.StringChunk;

/**
 * The artist of this song. You can maybe map this to an IDv2/IDv3 Tag.
 */
public class SongArtist extends StringChunk {
    
    public SongArtist() {
        this(null);
    }
    
    public SongArtist(String value) {
        super("asar", "daap.songartist", value);
    }
}
