
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.IntChunk;

/**
 * Used to tell the client its session-id
 */
public class SessionId extends IntChunk {
    
    public SessionId() {
        this(0);
    }
    
    public SessionId(int id) {
        super("mlid", "dmap.sessionid", id);
    }
}
