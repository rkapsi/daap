
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.ContainerChunk;
import java.util.ArrayList;

/**
 * Used by {@link de.kapsi.net.daap.ContentCodesImpl ContentCodesImpl}
 */
public class Dictionary extends ContainerChunk {
    
    public Dictionary() {
        super("mdcl", "dmap.dictionary");
    }
}
