
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SmartPlaylist extends BooleanChunk {
	
	public SmartPlaylist() {
		this(false);
	}
	
	public SmartPlaylist(boolean smart) {
		super("aeSP", "com.apple.itunes.smart-playlist", smart);
	}
}
