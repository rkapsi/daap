
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongDescription extends StringChunk {
	
	public static final String MPEG_AUDIO_FILE = "MPEG audio file";
	public static final String PLAYLIST_URL = "Playlist URL";
	
	public SongDescription() {
		this(null);
	}
	
	public SongDescription(String description) {
		super("asdt", "daap.songdescription", description);
	}
}
