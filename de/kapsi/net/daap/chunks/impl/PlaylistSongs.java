
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.ContainerChunk;

/**
 * Container for the <tt>/databases/id/containers/id/items</tt> request
 */
public class PlaylistSongs extends ContainerChunk {
    
    public PlaylistSongs() {
        super("apso", "daap.playlistsongs");
    }
}
