
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

public class BrowseArtistListing extends ContainerChunk {
	
	public BrowseArtistListing() {
		super("abar", "daap.browseartistlisting", new ArrayList());
	}
}
