
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.IntChunk;

public class ContentCodesNumber extends IntChunk {
    
    public ContentCodesNumber() {
        this(0);
    }
    
    public ContentCodesNumber(int number) {
        super("mcnm", "dmap.contentcodesnumber", number);
    }
}
