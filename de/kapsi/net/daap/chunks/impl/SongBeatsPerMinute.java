
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.ShortChunk;

/**
 * Beats per minute? One of those needless things...
 */
public class SongBeatsPerMinute extends ShortChunk {
    
    public SongBeatsPerMinute() {
        this(0);
    }
    
    public SongBeatsPerMinute(int bpm) {
        super("asbt", "daap.songbeatsperminute", bpm);
    }
}
