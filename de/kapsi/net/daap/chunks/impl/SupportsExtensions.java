
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.BooleanChunk;

/**
 * Unknown purpose. Used by {@link de.kapsi.net.daap.ServerInfoResponseImpl ServerInfoResponseImpl}
 */
public class SupportsExtensions extends BooleanChunk {
    
    public SupportsExtensions() {
        this(false);
    }
    
    public SupportsExtensions(boolean supports) {
        super("msex", "dmap.supportsextensions", supports);
    }
}
