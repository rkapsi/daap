
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.StringChunk;

/**
 * Unknown purpose.
 */
public class SongGrouping extends StringChunk {
    
    public SongGrouping() {
        this(null);
    }
    
    public SongGrouping(String grouping) {
        super("agrp","daap.songgrouping", grouping);
    }
}
