
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongDateModified extends DateChunk {
	
	public SongDateModified() {
		this(0);
	}
	
	public SongDateModified(int seconds) {
		super("asdm", "daap.songdatemodified", seconds);
	}
}
