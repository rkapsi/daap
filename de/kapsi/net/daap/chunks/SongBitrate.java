
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongBitrate extends ShortChunk {
	
	public SongBitrate() {
		this(0);
	}
	
	public SongBitrate(int bitrate) {
		super("asbr", "daap.songbitrate", bitrate);
	}
}
