
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.StringChunk;

/**
 * The name of an item (e.g. song or playlist)
 */
public class ItemName extends StringChunk {
    
    public ItemName() {
        this(null);
    }
    
    public ItemName(String name) {
        super("minm", "dmap.itemname", name);
    }
}
