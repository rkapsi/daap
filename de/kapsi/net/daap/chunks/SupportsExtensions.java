
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SupportsExtensions extends BooleanChunk {
		
	public SupportsExtensions() {
		this(false);
	}
	
	public SupportsExtensions(boolean supports) {
		super("msex", "dmap.supportsextensions", supports);
	}
}
