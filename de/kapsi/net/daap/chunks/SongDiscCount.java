
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongDiscCount extends ShortChunk {
	
	public SongDiscCount() {
		this(0);
	}
	
	public SongDiscCount(int count) {
		super("asdc", "daap.songdisccount", count);
	}
}
