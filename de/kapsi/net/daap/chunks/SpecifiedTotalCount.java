
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.IntChunk;

/**
 * The total number of songs, databases or whatever the library
 * has. This chunk usually appears together with ReturnedCount.
 *
 * @see ReturnedCount
 */
public class SpecifiedTotalCount extends IntChunk {
	
	public SpecifiedTotalCount() {
		this(0);
	}
	
	public SpecifiedTotalCount(int count) {
		super("mtco", "dmap.specifiedtotalcount", count);
	}
}
