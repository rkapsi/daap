
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class ReturnedCount extends IntChunk {
	
	public ReturnedCount() {
		this(0);
	}
	
	public ReturnedCount(int count) {
		super("mrco", "dmap.returnedcount", count);
	}
}
