
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class ServerRevision extends IntChunk {
	
	public ServerRevision() {
		this(0);
	}
	
	public ServerRevision(int count) {
		super("musr", "dmap.serverrevision", count);
	}
}
