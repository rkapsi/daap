
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.BooleanChunk;

/**
 * Unknown purpose. Used by {@link de.kapsi.net.daap.ServerInfoResponseImpl ServerInfoResponseImpl}
 */
public class SupportsIndex extends BooleanChunk {
    
    public SupportsIndex() {
        this(false);
    }
    
    public SupportsIndex(boolean supports) {
        super("msix", "dmap.supportsindex", supports);
    }
}
