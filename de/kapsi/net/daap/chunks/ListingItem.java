
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

public class ListingItem extends ContainerChunk {
	
	public ListingItem() {
		super("mlit", "dmap.listingitem", new ArrayList());
	}
}
