
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongAlbum extends StringChunk {
	
	public SongAlbum() {
		this(null);
	}
	
	public SongAlbum(String value) {
		super("asal", "daap.songalbum", value);
	}
}   
