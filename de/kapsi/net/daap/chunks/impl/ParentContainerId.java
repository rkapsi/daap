
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.IntChunk;

/**
 * Unknown purpose
 */
public class ParentContainerId extends IntChunk {
    
    public ParentContainerId() {
        this(0);
    }
    
    public ParentContainerId(int id) {
        super("mpco", "dmap.parentcontainerid", id);
    }
}
