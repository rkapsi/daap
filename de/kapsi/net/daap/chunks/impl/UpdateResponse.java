
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.ContainerChunk;
import java.util.ArrayList;

/**
 * Container for the <tt>/update</tt> request.
 */
public class UpdateResponse extends ContainerChunk {
    
    public UpdateResponse() {
        super("mupd", "dmap.updateresponse");
    }
}
