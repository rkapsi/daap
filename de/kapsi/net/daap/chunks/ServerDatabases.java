
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

/**
 * Container for the <tt>/databases</tt> request
 */
public class ServerDatabases extends ContainerChunk {
    
    public ServerDatabases() {
        super("avdb", "daap.serverdatabases");
    }
}
