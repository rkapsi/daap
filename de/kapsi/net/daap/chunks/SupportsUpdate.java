
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.BooleanChunk;

/**
 * Unknown purpose. Used by {@link de.kapsi.net.daap.ServerInfoResponseImpl ServerInfoResponseImpl}
 */
public class SupportsUpdate extends BooleanChunk {
	
	public SupportsUpdate() {
		this(false);
	}
	
	public SupportsUpdate(boolean supports) {
		super("msup", "dmap.supportsupdate", supports);
	}
}
