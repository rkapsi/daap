
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongUserRating extends ByteChunk {
	
	public SongUserRating() {
		this(0);
	}
	
	public SongUserRating(int rating) {
		super("asur", "daap.songuserrating", rating);
	}
}
