
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

/**
 * Unknown purpose.
 */
public class Container extends ContainerChunk {
	
	public Container() {
		super("mcon", "dmap.container", new ArrayList());
	}
}
