
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class Status extends IntChunk {
	
	public Status() {
		this(0);
	}
	
	public Status(int status) {
		super("mstt", "dmap.status", status);
	}
}
