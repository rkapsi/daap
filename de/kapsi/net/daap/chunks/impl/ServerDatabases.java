
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.ContainerChunk;

/**
 * Container for the <tt>/databases</tt> request
 */
public class ServerDatabases extends ContainerChunk {
    
    public ServerDatabases() {
        super("avdb", "daap.serverdatabases");
    }
}
