
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

/**
 * Container for deleted item IDs.
 *
 * @see Listing
 */
public class DeletedIdListing extends ContainerChunk {
	
	public DeletedIdListing() {
		super("mudl", "dmap.deletedidlisting", new ArrayList());
	}
}
