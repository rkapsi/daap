
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.ContainerChunk;

/**
 * Container for the <tt>/server-info</tt> request
 */
public class ServerInfoResponse extends ContainerChunk {
    
    public ServerInfoResponse() {
        super("msrv", "dmap.serverinforesponse");
    }
}
