
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

/**
 * Used to group ListingItems objects. 
 *
 * @see ListingItem
 */
public class Listing extends ContainerChunk {
	
	public Listing() {
		super("mlcl", "dmap.listing", new ArrayList());
	}
}
