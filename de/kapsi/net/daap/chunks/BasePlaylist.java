
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class BasePlaylist extends ByteChunk {
	
	public BasePlaylist() {
		this(0);
	}
	
	public BasePlaylist(int playlist) {
		super("abpl", "daap.baseplaylist", playlist);
	}
}
