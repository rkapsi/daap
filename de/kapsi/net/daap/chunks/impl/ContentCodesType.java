
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.ShortChunk;

public class ContentCodesType extends ShortChunk {
    
    public ContentCodesType() {
        this(0);
    }
    
    public ContentCodesType(int type) {
        super("mcty", "dmap.contentcodestype", type);
    }
}
