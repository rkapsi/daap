
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.BooleanChunk;

/**
 * Unknown purpose. Used by {@link de.kapsi.net.daap.ServerInfo ServerInfo}
 */
public class SupportsResolve extends BooleanChunk {
	
	public SupportsResolve() {
		this(false);
	}
	
	public SupportsResolve(boolean supports) {
		super("msrs", "dmap.supportsresolve", supports);
	}
}
