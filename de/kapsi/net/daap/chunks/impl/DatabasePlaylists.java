
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.ContainerChunk;

/**
 * Container for the <tt>/databases/id/containers</tt> request.
 */
public class DatabasePlaylists extends ContainerChunk {
    
    public DatabasePlaylists() {
        super("aply", "daap.databaseplaylists");
    }
}
