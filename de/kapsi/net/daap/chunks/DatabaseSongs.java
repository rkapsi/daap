
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

/**
 * Container for the <tt>/databases/id/items</tt> request.
 */
public class DatabaseSongs extends ContainerChunk {
	
	public DatabaseSongs() {
		super("adbs", "dapp.databasesongs", new ArrayList());
	}
}
