
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongDataKind extends ByteChunk {
	
	public SongDataKind() {
		this(0);
	}
	
	public SongDataKind(int kind) {
		super("asdk", "daap.songdatakind", kind);
	}
}
