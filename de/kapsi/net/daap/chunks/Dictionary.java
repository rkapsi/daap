
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

/**
 * Used by {@link de.kapsi.net.daap.ContentCodesImpl ContentCodesImpl}
 */
public class Dictionary extends ContainerChunk {
	
	public Dictionary() {
		super("mdcl", "dmap.dictionary", new ArrayList());
	}
}
