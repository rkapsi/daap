
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class DmapProtocolVersion extends VersionChunk {
	
	public DmapProtocolVersion() {
		this(0);
	}
	
	public DmapProtocolVersion(int version) {
		super("mpro", "dmap.protocolversion", version);
	}
}
