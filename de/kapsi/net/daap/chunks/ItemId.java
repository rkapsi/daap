
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class ItemId extends IntChunk {
	
	public ItemId() {
		this(0);
	}
	
	public ItemId(int itemId) {
		super("miid", "dmap.itemid", itemId);
	}
}
