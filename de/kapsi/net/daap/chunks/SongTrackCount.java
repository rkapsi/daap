
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongTrackCount extends ShortChunk {
	
	public SongTrackCount() {
		this(0);
	}
	
	public SongTrackCount(int count) {
		super("astc", "daap.songtrackcount", count);
	}
}
