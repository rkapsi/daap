
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

public class BrowseComposerListing extends ContainerChunk {
	
	public BrowseComposerListing() {
		super("abcp", "daap.browsecomposerlisting", new ArrayList());
	}
}
