
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

public class UpdateResponse extends ContainerChunk {
	
	public UpdateResponse() {
		super("mupd", "dmap.updateresponse", new ArrayList());
	}
}
