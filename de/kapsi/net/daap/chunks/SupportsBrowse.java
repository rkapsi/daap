
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SupportsBrowse extends BooleanChunk {
		
	public SupportsBrowse() {
		this(false);
	}
	
	public SupportsBrowse(boolean supports) {
		super("msbr", "dmap.supportsbrowse", supports);
	}
}
