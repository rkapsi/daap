
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.VersionChunk;

/**
 * Unknown purpose. Used by {@link de.kapsi.net.daap.ServerInfoResponseImpl ServerInfoResponseImpl}
 */
public class DaapProtocolVersion extends VersionChunk {
    
    public DaapProtocolVersion() {
        this(0);
    }
    
    public DaapProtocolVersion(int version) {
        super("apro", "daap.protocolversion", version);
    }
    
    public DaapProtocolVersion(int major, int minor, int patch) {
        super("apro", "daap.protocolversion", major, minor, patch);
    }
}
