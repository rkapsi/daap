
package de.kapsi.net.daap.chunks;

import java.math.BigInteger;
import java.io.OutputStream;
import java.io.IOException;

/**
 * A long chunk is a 8 byte value encoded as Hex String.
 * The greatest number is "FFFFFFFFFFFFFFFF".
 */
public class LongChunk extends AbstractChunk {
    
    private static final BigInteger MAX_VALUE 
        = new BigInteger("FFFFFFFFFFFFFFFF", 16);
    
    private BigInteger value;
    
    protected LongChunk(String type, String name, long value) {
        super(type, name);
        setValue(value);
    }
    
    protected LongChunk(String type, String name, String value) {
        super(type, name);
        setValue(value);
    }
    
    protected LongChunk(String type, String name, BigInteger value) {
        super(type, name);
        setValue(value);
    }
    
    public BigInteger getValue() {
        return value;
    }
    
    public void setValue(long value) {
        setValue(new BigInteger(Long.toHexString(value), 16));
    }
    
    public void setValue(String value) {
        setValue(new BigInteger(value, 16));
    }
    
    public void setValue(BigInteger value) {
        if (value == null || value.compareTo(MAX_VALUE) > 0) {
            throw new IllegalArgumentException();
        }
        
        this.value = value;
    }
    
    /**
     * Length is 8 byte
     */
    public int getLength() {
        return 8;
    }
    
    /**
     * Returns <tt>Chunk.LONG_TYPE</tt>
     */
    public int getType() {
        return Chunk.LONG_TYPE;
    }
    
    public void serialize(OutputStream out) throws IOException {
        
        super.serialize(out);
        
        byte[] bytes = value.toByteArray();
        byte[] tmp = new byte[getLength()];
        
        int i = getLength()-1;
        int j = bytes.length-1;
        
        while(i >= 0 && j >= 0) {
            tmp[i] = bytes[j];
            
            i--;
            j--;
        }
        
        out.write(tmp, 0, tmp.length);
    }
    
    public String toString() {
        return super.toString() + "=" + getValue();
    }
}
