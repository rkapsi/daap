
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.IntChunk;

/**
 * The ID of an item. This value must be unique for a certain set
 * of items (e.g. songs) and != 0
 */
public class ItemId extends IntChunk {
    
    public ItemId() {
        this(0);
    }
    
    public ItemId(int itemId) {
        super("miid", "dmap.itemid", itemId);
    }
}
