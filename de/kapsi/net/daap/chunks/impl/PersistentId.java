
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.LongChunk;
import java.math.BigInteger;

/**
 * In theory used for global unique IDs and afaik only required
 * for the <tt>/resolve</tt> request.
 */
public class PersistentId extends LongChunk {
    
    public PersistentId() {
        this(0L);
    }
    
    public PersistentId(long id) {
        super("mper", "dmap.persistentid", id);
    }
    
    public PersistentId(String id) {
        super("mper", "dmap.persistentid", id);
    }
    
    public PersistentId(BigInteger id) {
        super("mper", "dmap.persistentid", id);
    }
}
