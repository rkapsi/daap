
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.IntChunk;

/**
 * Unknown purpose
 */
public class ParentContainerId extends IntChunk {
	
	public ParentContainerId() {
		this(0);
	}
	
	public ParentContainerId(int id) {
		super("mpco", "dmap.parentcontainerid", id);
	}
}
