
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

/**
 * In seconds!
 */
public class SongStopTime extends IntChunk {
	
	public SongStopTime() {
		this(0);
	}
	
	public SongStopTime(int time) {
		super("assp", "daap.songstoptime", time);
	}
}
