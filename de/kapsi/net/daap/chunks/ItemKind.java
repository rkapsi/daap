
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class ItemKind extends ByteChunk {
	
	public ItemKind() {
		this(0);
	}
	
	public ItemKind(int kind) {
		super("mikd", "dmap.itemkind", kind);
	}
}
