
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class ContentCodesNumber extends IntChunk {
    
    public ContentCodesNumber() {
        this(0);
    }
    
    public ContentCodesNumber(int number) {
        super("mcnm", "dmap.contentcodesnumber", number);
    }
}
