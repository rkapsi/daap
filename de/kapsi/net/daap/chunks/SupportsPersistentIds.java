
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SupportsPersistentIds extends BooleanChunk {
		
	public SupportsPersistentIds() {
		this(false);
	}
	
	public SupportsPersistentIds(boolean supports) {
		super("mspi", "dmap.supportspersistentids", supports);
	}
}
