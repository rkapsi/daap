
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.StringChunk;

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
