
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SupportsUpdate extends BooleanChunk {
	
	public SupportsUpdate() {
		this(false);
	}
	
	public SupportsUpdate(boolean supports) {
		super("msup", "dmap.supportsupdate", supports);
	}
}
