
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.VersionChunk;

/**
 * Unknown purpose. Used by {@link de.kapsi.net.daap.ServerInfoResponseImpl ServerInfoResponseImpl}
 */
public class DmapProtocolVersion extends VersionChunk {
    
    public static final int VERSION_1 = 0x00010000; // 1.0.0
    public static final int VERSION_2 = 0x00020000; // 2.0.0
    
    public DmapProtocolVersion() {
        this(0);
    }
    
    public DmapProtocolVersion(int version) {
        super("mpro", "dmap.protocolversion", version);
    }
}
