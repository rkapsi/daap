
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

public class Listing extends ContainerChunk {
	
	public Listing() {
		super("mlcl", "dmap.listing", new ArrayList());
	}
}
