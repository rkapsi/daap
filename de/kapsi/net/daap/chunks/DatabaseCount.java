
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class DatabaseCount extends IntChunk {
	
	public DatabaseCount() {
		this(0);
	}
	
	public DatabaseCount(int count) {
		super("msdc", "dmap.databasescount", count);
	}
}
