
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

public class PlaylistSongs extends ContainerChunk {
	
	public PlaylistSongs() {
		super("apso", "daap.playlistsongs", new ArrayList());
	}
}
