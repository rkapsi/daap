
package de.kapsi.net.daap;

import java.io.OutputStream;
import java.io.IOException;

/**
 * An implementation of a boolean chunk.
 */
public class BooleanChunk extends AbstractChunk {
    
    private boolean value;
    
    protected BooleanChunk(String type, String name, boolean value) {
        super(type, name);
        this.value = value;
    }
    
    public boolean getValue() {
        return value;
    }
    
    public void setValue(boolean value) {
        this.value = value;
    }
    
    /**
     * Length is 1 byte
     */
    public int getLength() {
        return 1;
    }
    
    /**
     * Returns <tt>Chunk.BOOLEAN_TYPE</tt>
     */
    public int getType() {
        return Chunk.BOOLEAN_TYPE;
    }
    
    public void serialize(OutputStream out) throws IOException {
        super.serialize(out);
        out.write((getValue()) ? (byte)1 : (byte)0);
    }
    
    public String toString() {
        return super.toString() + "=" + value;
    }
}
