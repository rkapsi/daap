
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SpecifiedTotalCount extends IntChunk {
	
	public SpecifiedTotalCount() {
		this(0);
	}
	
	public SpecifiedTotalCount(int count) {
		super("mtco", "dmap.specifiedtotalcount", count);
	}
}
