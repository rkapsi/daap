
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongDisabled extends BooleanChunk {
	
	public SongDisabled() {
		this(false);
	}
	
	public SongDisabled(boolean disabled) {
		super("asdb", "daap.songdisabled", disabled);
	}
}
