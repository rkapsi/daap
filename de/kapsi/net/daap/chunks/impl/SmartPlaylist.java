
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.BooleanChunk;

public class SmartPlaylist extends BooleanChunk {
    
    public SmartPlaylist() {
        this(false);
    }
    
    public SmartPlaylist(boolean smart) {
        super("aeSP", "com.apple.itunes.smart-playlist", smart);
    }
}
