
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ShortChunk;

/**
 * Beats per minute? One of those needless things...
 */
public class SongBeatsPerMinute extends ShortChunk {
	
	public SongBeatsPerMinute() {
		this(0);
	}
	
	public SongBeatsPerMinute(int bpm) {
		super("asbt", "daap.songbeatsperminute", bpm);
	}
}
