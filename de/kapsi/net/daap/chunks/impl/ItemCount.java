
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.IntChunk;

/**
 *
 */
public class ItemCount extends IntChunk {
    
    public ItemCount() {
        this(0);
    }
    
    public ItemCount(int count) {
        super("mimc", "dmap.itemcount", count);
    }
}
