
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

/**
 * Container for the <tt>/databases/id/containers/id/items</tt> request
 */
public class PlaylistSongs extends ContainerChunk {
    
    public PlaylistSongs() {
        super("apso", "daap.playlistsongs");
    }
}
