
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

public class ServerInfoResponse extends ContainerChunk {
	
	public ServerInfoResponse() {
		super("msrv", "dmap.serverinforesponse", new ArrayList());
	}
}
