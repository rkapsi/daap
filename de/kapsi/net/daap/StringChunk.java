
package de.kapsi.net.daap;

import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
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
    
    public int getLength() {
        return (bytes != null) ? bytes.length : 0;
    }
    
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
