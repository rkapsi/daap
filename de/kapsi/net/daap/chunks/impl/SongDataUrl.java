
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.StringChunk;

/**
 * Unknown
 */
public class SongDataUrl extends StringChunk {
    
    /**
     *
     */
    public SongDataUrl() {
        this(null);
    }
    
    /**
     * @param url
     */
    public SongDataUrl(String url) {
        super("asul", "daap.songdataurl", url);
    }
}
