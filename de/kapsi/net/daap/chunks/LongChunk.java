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
