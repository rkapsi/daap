
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class TimeoutInterval extends IntChunk {
	
	public TimeoutInterval() {
		this(0);
	}
	
	public TimeoutInterval(int interval) {
		super("mstm", "dmap.timeoutinterval", interval);
	}
}
