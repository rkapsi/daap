
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongCompilation extends BooleanChunk {
	
	public SongCompilation() {
		this(false);
	}
	
	public SongCompilation(boolean comp) {
		super("asco", "daap.songcompilation", comp);
	}
}
