
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongTrackNumber extends ShortChunk {
	
	public SongTrackNumber() {
		this(0);
	}
	
	public SongTrackNumber(int num) {
		super("astn", "daap.songtracknumber", num);
	}
}
