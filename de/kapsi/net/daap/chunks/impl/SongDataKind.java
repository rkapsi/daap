
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.ByteChunk;

/**
 * This class describes if a song is either
 * a Radio stream or DAAP stream.
 */
public class SongDataKind extends ByteChunk {
    
    public static final int RADIO_STREAM    = 1;
    public static final int DAAP_STREAM     = 2;
    
    public SongDataKind() {
        this(DAAP_STREAM);
    }
    
    public SongDataKind(int kind) {
        super("asdk", "daap.songdatakind", kind);
    }
}
