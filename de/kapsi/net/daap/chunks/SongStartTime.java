
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

/**
 * In seconds!
 */
public class SongStartTime extends IntChunk {
	
	public SongStartTime() {
		this(0);
	}
	
	public SongStartTime(int time) {
		super("asst", "daap.songstarttime", time);
	}
}
