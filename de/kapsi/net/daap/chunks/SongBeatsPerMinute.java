
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongBeatsPerMinute extends ShortChunk {
	
	public SongBeatsPerMinute() {
		this(0);
	}
	
	public SongBeatsPerMinute(int bpm) {
		super("asbt", "daap.songbeatsperminute", bpm);
	}
}
