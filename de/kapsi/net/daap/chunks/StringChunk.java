/* 
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004 Roger Kapsi, info at kapsi dot de
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.kapsi.net.daap.chunks;

import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class implements a String chunk. DAAP Strings are
 * encoded in UTF-8.
 * <p>Note: <code>null</code> is valid value for DAAP and should
 * be favored over "" for empty Strings as this saves 
 * 1 byte per String.</p>
 *
 * @author  Roger Kapsi
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
     * Length is <code>String.getBytes("UTF-8").length</code>
     */
    public int getLength() {
        return (bytes != null) ? bytes.length : 0;
    }
    
    /**
     * Returns {@see Chunk.STRING_TYPE}
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
