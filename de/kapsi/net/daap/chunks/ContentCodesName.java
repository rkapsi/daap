
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class ContentCodesName extends StringChunk {
    
    public ContentCodesName() {
        this(null);
    }
    
    public ContentCodesName(String name) {
        super("mcna", "dmap.contentcodesname", name);
    }
}
