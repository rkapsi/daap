
package de.kapsi.net.daap.chunks;

import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class implements a String chunk. DAAP Strings are
 * encoded in UTF-8.<p>
 * Note: <tt>null</tt> is valid value for DAAP and should
 * be favored over "" for empty Strings as this saves 
 * 1 byte per String.
 */
public class StringChunk extends AbstractChunk {
    
    private static final Log LOG = LogFactory.getLog(StringChunk.class);
    
    private byte[] bytes;
    
    protected StringChunk(String type, String name, String value) {
        super(type, name);
        setValue(value);
    }
    
    public String getValue() {
        if (bytes != null) {
            try {
                return new String(bytes, "UTF-8");
            } catch (UnsupportedEncodingException err) {
                LOG.error(err); // shouldn't happen but who knows!?
            }
        }
        return null;
    }
    
    public void setValue(String value) {
        if (value != null) {
            try {
                bytes = value.getBytes("UTF-8");
            } catch (UnsupportedEncodingException err) {
                LOG.error(err); // shouldn't happen but who knows!?
            }
        } else {
            bytes = null;
        }
    }
    
    /**
     * Length is <tt>String.getBytes("UTF-8").length</tt>
     */
    public int getLength() {
        return (bytes != null) ? bytes.length : 0;
    }
    
    /**
     * Returns <tt>Chunk.STRING_TYPE</tt>
     */
    public int getType() {
        return Chunk.STRING_TYPE;
    }
    
    public void serialize(OutputStream out) throws IOException {
        
        super.serialize(out);
        
        if (bytes != null) {
            out.write(bytes, 0, bytes.length);
        }
    }
    
    public String toString() {
        return super.toString() + "=" + getValue();
    }
}
