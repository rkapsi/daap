
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

public class BrowseGenreListing extends ContainerChunk {
	
	public BrowseGenreListing() {
		super("abgn", "daap.browsegenrelisting", new ArrayList());
	}
}
