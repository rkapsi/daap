
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongDiscNumber extends ShortChunk {
	
	public SongDiscNumber() {
		this(0);
	}
	
	public SongDiscNumber(int count) {
		super("asdn", "daap.songdiscnumber", count);
	}
}
