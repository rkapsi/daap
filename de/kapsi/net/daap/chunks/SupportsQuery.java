
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.BooleanChunk;

/**
 * Unknown purpose. Used by {@link de.kapsi.net.daap.ServerInfo ServerInfo}
 */
public class SupportsQuery extends BooleanChunk {
	
	public SupportsQuery() {
		this(false);
	}
	
	public SupportsQuery(boolean supports) {
		super("msqy", "dmap.supportsquery", supports);
	}
}
