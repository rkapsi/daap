
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ByteChunk;

/**
 * Unknown purpose. This chunk seems to be always 2
 */
public class ItemKind extends ByteChunk {
    
    public static final int KIND_2 = 2;
    
    public ItemKind() {
        this(0);
    }
    
    public ItemKind(int kind) {
        super("mikd", "dmap.itemkind", kind);
    }
}
