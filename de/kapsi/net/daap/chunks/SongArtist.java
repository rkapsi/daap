
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongArtist extends StringChunk {
		
	public SongArtist() {
		this(null);
	}
	
	public SongArtist(String value) {
		super("asar", "daap.songartist", value);
	}
}
