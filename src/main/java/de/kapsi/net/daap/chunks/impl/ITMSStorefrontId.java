package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.UIntChunk;

public class ITMSStorefrontId extends UIntChunk {
    
    public ITMSStorefrontId() {
        this(0);
    }
    
    public ITMSStorefrontId(long stroreFrontId) {
        super("aeSF", "com.apple.itunes.itms-storefrontid", stroreFrontId);
    }
}
