
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

/**
 * In Milliseconds!
 */
public class SongTime extends IntChunk {
	
	public SongTime() {
		this(0);
	}
	
	public SongTime(int time) {
		super("astm", "daap.songtime", time);
	}
}
