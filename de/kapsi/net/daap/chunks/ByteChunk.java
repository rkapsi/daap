
package de.kapsi.net.daap.chunks;

import java.io.OutputStream;
import java.io.IOException;

/**
 * An implementation of a byte chunk
 */
public class ByteChunk extends AbstractChunk {
    
    private byte value;
    
    protected ByteChunk(String type, String name, int value) {
        super(type, name);
        setValue(value);
    }
    
    public byte getValue() {
        return value;
    }
    
    /**
     * Note: although value is an int (I don't like casting 
     * primitives) it is masked internally with 0xFF and 
     * casted to a byte.
     */
    public void setValue(int value) {
        this.value = (byte)(value & 0xFF);
    }
    
    /**
     * Length is 1 byte
     */
    public int getLength() {
        return 1;
    }
    
    /**
     * Returns Chunk.BYTE_TYPE
     */
    public int getType() {
        return Chunk.BYTE_TYPE;
    }
    
    public void serialize(OutputStream out) throws IOException {
        super.serialize(out);
        out.write(getValue());
    }
    
    public String toString() {
        return super.toString() + "=" + value;
    }
}
