
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongSize extends IntChunk {
	
	public SongSize() {
		this(0);
	}
	
	public SongSize(int size) {
		super("assz", "daap.songsize", size);
	}
}
