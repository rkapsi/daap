
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.StringChunk;

/**
 * Unknown purpose.
 */
public class StatusString extends StringChunk {
    
    public StatusString() {
        this(null);
    }
    
    public StatusString(String statusString) {
        super("msts", "dmap.statusstring", statusString);
    }
}
