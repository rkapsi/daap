
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongDateAdded extends DateChunk {
	
	public SongDateAdded() {
		this(0);
	}
	
	public SongDateAdded(int seconds) {
		super("asda", "daap.songdateadded", seconds);
	}
}
