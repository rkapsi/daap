
package de.kapsi.net.daap;

import java.io.OutputStream;
import java.io.IOException;

public interface Chunk {
    
    public static final int BYTE_TYPE       = 1;
    
    // That's correct. A boolean is a byte! 0 = false, !0 = true
    // (i.e. everything else except 0). As we're on the server side
    // and do not have to deal with parsing etc. it's not important
    // for us but keep it in mind.
    public static final int BOOLEAN_TYPE    = BYTE_TYPE;
    
    public static final int SHORT_TYPE      = 3;
    public static final int INT_TYPE        = 5;
    public static final int LONG_TYPE       = 7;
    public static final int STRING_TYPE     = 9;
    public static final int DATE_TYPE       = 10;
    public static final int VERSION_TYPE    = 11;
    public static final int CONTAINER_TYPE  = 12;
    
    /**
     * Returns the total size (header+payload) of a chunk
     * in bytes.
     */
    public int getSize();
    
    /**
     * Returns the payload of a chunk in bytes.
     */
    public int getLength();
    
    /**
     * Returns the type of a chunk (BOOLEAN_TYPE etc).
     */
    public int getType();
    
    /**
     * Writes 'this' to 'out'
     */
    public void serialize(OutputStream out) throws IOException;
}
