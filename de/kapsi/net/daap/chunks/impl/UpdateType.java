
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.BooleanChunk;

/**
 * This chunk is used to indicate if a response is
 * either an update or a full response. The first request
 * is always followed by a full response (e.g. the list of
 * all songs in the library) and thenceforward it's awlays
 * an update (a diff between client's and server's library).
 */
public class UpdateType extends BooleanChunk {
    
    /**
     * Creates a new UpdateType which is initialized with <tt>false</tt>.
     * You can change this value with {@see #setValue(boolean)}.
     */
    public UpdateType() {
        this(false);
    }
    
    /**
     * Creates a new UpdateType with the assigned rating.
     * You can change this value with {@see #setValue(boolean)}.
     * @param <tt>updatetype</tt> either false or true
     */
    public UpdateType(boolean updatetype) {
        super("muty", "dmap.updatetype", updatetype);
    }
}
