
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class ItemName extends StringChunk {
	
	public ItemName() {
		this(null);
	}
	
	public ItemName(String name) {
		super("minm", "dmap.itemname", name);
	}
}
