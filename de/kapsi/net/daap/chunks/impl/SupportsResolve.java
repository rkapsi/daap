
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.BooleanChunk;

/**
 * Unknown purpose. Used by {@link de.kapsi.net.daap.ServerInfoResponseImpl ServerInfoResponseImpl}
 */
public class SupportsResolve extends BooleanChunk {
    
    public SupportsResolve() {
        this(false);
    }
    
    public SupportsResolve(boolean supports) {
        super("msrs", "dmap.supportsresolve", supports);
    }
}
