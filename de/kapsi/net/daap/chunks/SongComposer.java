
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongComposer extends StringChunk {
	
	public SongComposer() {
		this(null);
	}
	
	public SongComposer(String composer) {
		super("ascp", "daap.songcomposer", composer);
	}
}
