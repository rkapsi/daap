
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class ContentCodesType extends ShortChunk {
    
    public ContentCodesType() {
        this(0);
    }
    
    public ContentCodesType(int type) {
        super("mcty", "dmap.contentcodestype", type);
    }
}
