
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class ContainerCount extends IntChunk {
	
	public ContainerCount() {
		this(0);
	}
	
	public ContainerCount(int count) {
		super("mctc", "dmap.containercount", count);
	}
}
