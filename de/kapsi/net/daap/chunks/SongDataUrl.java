
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongDataUrl extends StringChunk {
	
	public SongDataUrl() {
		this(null);
	}
	
	public SongDataUrl(String url) {
		super("asul", "daap.songdataurl", url);
	}
}
