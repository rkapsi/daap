
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

/**
 * Unknown purpose.
 */
public class Resolve extends ContainerChunk {
	
	public Resolve() {
		super("arsv", "daap.resolve", new ArrayList());
	}
}
