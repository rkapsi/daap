
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.BooleanChunk;

/**
 * Unknown purpose. Used by {@link de.kapsi.net.daap.ServerInfoResponseImpl ServerInfoResponseImpl}
 */
public class SupportsQuery extends BooleanChunk {
    
    public SupportsQuery() {
        this(false);
    }
    
    public SupportsQuery(boolean supports) {
        super("msqy", "dmap.supportsquery", supports);
    }
}
