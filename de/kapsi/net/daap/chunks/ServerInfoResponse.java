
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

/**
 * Container for the <tt>/server-info</tt> request
 */
public class ServerInfoResponse extends ContainerChunk {
    
    public ServerInfoResponse() {
        super("msrv", "dmap.serverinforesponse");
    }
}
