
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.VersionChunk;

/**
 * Unknown purpose. Used by {@link de.kapsi.net.daap.ServerInfoResponseImpl ServerInfoResponseImpl}
 */
public class DaapProtocolVersion extends VersionChunk {
	
	public static final int VERSION_1 = 0x00010000; // 1.0.0
	public static final int VERSION_2 = 0x00020000; // 2.0.0
	
	public DaapProtocolVersion() {
		this(0);
	}
	
	public DaapProtocolVersion(int version) {
		super("apro", "daap.protocolversion", version);
	}
}
