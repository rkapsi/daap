
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class DaapProtocolVersion extends VersionChunk {
	
	public DaapProtocolVersion() {
		this(0);
	}
	
	public DaapProtocolVersion(int version) {
		super("apro", "daap.protocolversion", version);
	}
}
