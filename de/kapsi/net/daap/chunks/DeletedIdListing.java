
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

public class DeletedIdListing extends ContainerChunk {
	
	public DeletedIdListing() {
		super("mudl", "dmap.deletedidlisting", new ArrayList());
	}
}
