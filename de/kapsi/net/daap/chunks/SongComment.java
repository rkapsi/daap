
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongComment extends StringChunk {
	
	public SongComment() {
		this(null);
	}
	
	public SongComment(String comment) {
		super("ascm", "daap.songcomment", comment);
	}
}
