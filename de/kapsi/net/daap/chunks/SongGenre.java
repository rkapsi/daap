
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongGenre extends StringChunk {
	
	public SongGenre() {
		this(null);
	}
	
	public SongGenre(String genre) {
		super("asgn", "daap.songgenre", genre);
	}
}
