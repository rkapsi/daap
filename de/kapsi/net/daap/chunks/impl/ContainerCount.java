
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.IntChunk;

public class ContainerCount extends IntChunk {
    
    public ContainerCount() {
        this(0);
    }
    
    public ContainerCount(int count) {
        super("mctc", "dmap.containercount", count);
    }
}
