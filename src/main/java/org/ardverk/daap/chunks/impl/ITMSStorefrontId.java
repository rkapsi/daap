package org.ardverk.daap.chunks.impl;

import org.ardverk.daap.chunks.UIntChunk;

public class ITMSStorefrontId extends UIntChunk {
    
    public ITMSStorefrontId() {
        this(0);
    }
    
    public ITMSStorefrontId(long stroreFrontId) {
        super("aeSF", "com.apple.itunes.itms-storefrontid", stroreFrontId);
    }
}
