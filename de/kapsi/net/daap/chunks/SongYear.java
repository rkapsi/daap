
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongYear extends ShortChunk {
	
	public SongYear() {
		this(0);
	}
	
	public SongYear(int year) {
		super("asyr", "daap.songyear", year);
	}
}
