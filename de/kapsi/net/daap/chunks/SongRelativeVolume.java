
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongRelativeVolume extends ByteChunk {
	
	public SongRelativeVolume() {
		this(0);
	}
	
	public SongRelativeVolume(int volume) {
		super("asrv", "daap.songrelativevolume", volume);
	}
}
