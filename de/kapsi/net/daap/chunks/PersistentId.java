
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class PersistentId extends LongChunk {
	
	public PersistentId() {
		this("0");
	}
	
	public PersistentId(long id) {
		this(Long.toString(id));
	}
	
	public PersistentId(String id) {
		super("mper", "dmap.persistentid", id);
	}
}
