
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.StringChunk;

public class ContentCodesName extends StringChunk {
    
    public ContentCodesName() {
        this(null);
    }
    
    public ContentCodesName(String name) {
        super("mcna", "dmap.contentcodesname", name);
    }
}
