
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongSampleRate extends IntChunk {
	
	public SongSampleRate() {
		this(0);
	}
	
	public SongSampleRate(int rate) {
		super("assr", "daap.songsamplerate", rate);
	}
}
