
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.VersionChunk;

/**
 * Unknown purpose. Used by {@link de.kapsi.net.daap.ServerInfoResponseImpl ServerInfoResponseImpl}
 */
public class DmapProtocolVersion extends VersionChunk {
    
    public DmapProtocolVersion() {
        this(0);
    }
    
    public DmapProtocolVersion(int version) {
        super("mpro", "dmap.protocolversion", version);
    }
    
    public DmapProtocolVersion(int major, int minor, int patch) {
        super("mpro", "dmap.protocolversion", major, minor, patch);
    }
}
