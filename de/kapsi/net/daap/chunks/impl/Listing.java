
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.ContainerChunk;

/**
 * Used to group ListingItems objects.
 *
 * @see ListingItem
 */
public class Listing extends ContainerChunk {
    
    public Listing() {
        super("mlcl", "dmap.listing");
    }
}
