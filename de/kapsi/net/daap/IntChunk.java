
package de.kapsi.net.daap;

import java.io.OutputStream;
import java.io.IOException;

/**
 * This class is an implementation of an 4 byte
 * int chnunk.
 */
public class IntChunk extends AbstractChunk {
    
    private int value;
    
    protected IntChunk(String type, String name, int value) {
        super(type, name);
        
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value = value;
    }
    
    /**
     * Length is 4 bytes
     */
    public int getLength() {
        return 4;
    }
    
    /**
     * Returns <tt>Chunk.INT_TYPE</tt>
     */
    public int getType() {
        return Chunk.INT_TYPE;
    }
    
    public void serialize(OutputStream out) throws IOException {
        
        super.serialize(out);
        
        byte[] dst = new byte[4];
        ByteUtil.toByteBE(value, dst, 0);
        
        out.write(dst, 0, dst.length);
    }
    
    public String toString() {
        return super.toString() + "=" + value;
    }
}
