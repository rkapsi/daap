
package de.kapsi.net.daap.chunks;

import java.io.OutputStream;
import java.io.IOException;

import de.kapsi.net.daap.ByteUtil;

/**
 * An abstract base class whereof 
 */
public abstract class AbstractChunk implements Chunk {
    
    // 4 bytes for the content code and 4 bytes for
    // the size of the playload
    private static final int HEADER_SIZE = 4+4;
    
    private String contentCode;
    private String name;
    
    protected AbstractChunk(String contentCode, String name) {
        
        if (contentCode.length() != 4) {
            throw new IndexOutOfBoundsException("Content Code must have 4 characters");
        }
        
        this.contentCode = contentCode;
        this.name = name;
    }
    
    /**
     * Returns the 4 charecter content code
     */
    public String getContentCode() {
        return contentCode;
    }
    
    /**
     * Returns the name of this chunk
     */
    public String getName() {
        return name;
    }
    
    public int getSize() {
        return HEADER_SIZE + getLength();
    }
    
    public abstract int getLength();
    public abstract int getType();
    
    public void serialize(OutputStream out) throws IOException {
        byte[] buffer = new byte[HEADER_SIZE];
        
        ByteUtil.toContentCodeBytes(contentCode, buffer, 0);
        ByteUtil.toByteBE(getLength(), buffer, 4);
        
        out.write(buffer, 0, buffer.length);
    }
    
    public String toString() {
        return name + "('" + contentCode + "')";
    }
}
