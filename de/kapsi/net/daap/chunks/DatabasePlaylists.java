
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

/**
 * Container for the <tt>/databases/id/containers</tt> request.
 */
public class DatabasePlaylists extends ContainerChunk {
	
	public DatabasePlaylists() {
		super("aply", "daap.databaseplaylists", new ArrayList());
	}
}
