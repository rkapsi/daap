
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

public class BrowseAlbumListing extends ContainerChunk {
	
	public BrowseAlbumListing() {
		super("abal", "daap.browsealbumlisting", new ArrayList());
	}
}
