
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

/**
 * Unknown purpose.
 */
public class Bag extends ContainerChunk {
	
	public Bag() {
		super("mbcl", "dmap.bag", new ArrayList());
	}
}
