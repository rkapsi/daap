
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class StatusString extends StringChunk {
	
	public StatusString() {
		this(null);
	}
	
	public StatusString(String statusString) {
		super("msts", "dmap.statusstring", statusString);
	}
}
