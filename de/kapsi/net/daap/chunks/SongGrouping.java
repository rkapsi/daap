
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.StringChunk;

/**
 * Unknown purpose.
 */
public class SongGrouping extends StringChunk {
	
	public SongGrouping() {
		this(null);
	}
	
	public SongGrouping(String grouping) {
		super("agrp","daap.songgrouping", grouping);
	}
}
