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

import de.kapsi.net.daap.ByteUtil;

/**
 * A short is a 16bit value
 *
 * @author  Roger Kapsi
 */
public class ShortChunk extends AbstractChunk {
    
    private int value;
    
    protected ShortChunk(String type, String name, int value) {
        super(type, name);
        
        this.value = (value & 0xFFFF);
    }
    
    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value = (value & 0xFFFF);
    }
    
    /**
     * Length is 2 bytes
     */
    public int getLength() {
        return 2;
    }
    
    /**
     * Returns <tt>Chunk.SHORT_TYPE</tt>
     */
    public int getType() {
        return Chunk.SHORT_TYPE;
    }
    
    public void serialize(OutputStream out) throws IOException {
        
        super.serialize(out);
        
        byte[] dst= new byte[2];
        ByteUtil.toByte16BE(value, dst, 0);
        out.write(dst, 0, dst.length);
    }
    
    public String toString() {
        return super.toString() + "=" + value;
    }
}
