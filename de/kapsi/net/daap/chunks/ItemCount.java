
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.IntChunk;

/**
 * 
 */
public class ItemCount extends IntChunk {
	
	public ItemCount() {
		this(0);
	}
	
	public ItemCount(int count) {
		super("mimc", "dmap.itemcount", count);
	}
}
