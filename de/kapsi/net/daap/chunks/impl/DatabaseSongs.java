
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.ContainerChunk;

/**
 * Container for the <tt>/databases/id/items</tt> request.
 */
public class DatabaseSongs extends ContainerChunk {
    
    public DatabaseSongs() {
        super("adbs", "dapp.databasesongs");
    }
}
