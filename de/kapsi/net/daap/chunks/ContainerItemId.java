
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class ContainerItemId extends IntChunk {
	
	public ContainerItemId() {
		this(0);
	}
	
	public ContainerItemId(int id) {
		super("mcti", "dmap.containeritemid", id);
	}
}
