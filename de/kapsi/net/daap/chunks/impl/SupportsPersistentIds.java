
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.BooleanChunk;

/**
 * Unknown purpose. Used by {@link de.kapsi.net.daap.ServerInfoResponseImpl ServerInfoResponseImpl}
 */
public class SupportsPersistentIds extends BooleanChunk {
    
    public SupportsPersistentIds() {
        this(false);
    }
    
    public SupportsPersistentIds(boolean supports) {
        super("mspi", "dmap.supportspersistentids", supports);
    }
}
