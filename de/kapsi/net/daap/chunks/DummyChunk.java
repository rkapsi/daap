
package de.kapsi.net.daap.chunks;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The purpose of DummyChunk is to mask itself as an another
 * Chunk with the difference that DummyChunks cannot be
 * serialized. It's used to workaround some consistency
 * checks of the Library...
 */
public final class DummyChunk extends AbstractChunk {
    
    private final int type;
    
    /**
     *
     * @param chunk
     */    
    public DummyChunk(AbstractChunk chunk) {
        super(chunk.getContentCode(), chunk.getName());
        
        if (chunk instanceof DummyChunk)
            throw new RuntimeException("DummyChunk cannot be the ");
        
        type = chunk.getType();
    }
    
    /**
     * Returns always 0
     * @return 0
     */    
    public final int getSize() {
        return 0;
    }
    
    /**
     * Returns always 0
     * @return 0
     */    
    public final int getLength() {
        return 0;
    }
    
    /**
     * 
     * @return 0
     */
    public final int getType() {
        return type;
    }
    
    /**
     * DummyChunk doesn't write anything to <tt>out</tt>!!!
     */
    public final void serialize(OutputStream out) throws IOException {
    }
}
