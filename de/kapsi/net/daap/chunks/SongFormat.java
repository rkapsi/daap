
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongFormat extends StringChunk {
	
	public static final String MP3 = "mp3";
	public static final String PLS = "pls";
	
	public SongFormat() {
		this(null);
	}
	
	public SongFormat(String format) {
		super("asfm", "daap.songformat", format);
	}
}
