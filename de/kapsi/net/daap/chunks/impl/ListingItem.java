
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.ContainerChunk;
import java.util.ArrayList;

/**
 * This container is used to group Items. An such group repesents
 * for example a song.
 */
public class ListingItem extends ContainerChunk {
    
    public ListingItem() {
        super("mlit", "dmap.listingitem");
    }
}
