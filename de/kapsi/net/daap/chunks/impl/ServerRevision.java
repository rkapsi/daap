
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.IntChunk;

/**
 * Used for the <tt>/update</tt> request and represents
 * the current revisionNumber of the Library.
 */
public class ServerRevision extends IntChunk {
    
    public ServerRevision() {
        this(0);
    }
    
    public ServerRevision(int count) {
        super("musr", "dmap.serverrevision", count);
    }
}
