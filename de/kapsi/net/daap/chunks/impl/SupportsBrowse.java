
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.BooleanChunk;

/**
 * Unknown purpose. Used by {@link de.kapsi.net.daap.ServerInfoResponseImpl ServerInfoResponseImpl}
 */
public class SupportsBrowse extends BooleanChunk {
    
    public SupportsBrowse() {
        this(false);
    }
    
    public SupportsBrowse(boolean supports) {
        super("msbr", "dmap.supportsbrowse", supports);
    }
}
