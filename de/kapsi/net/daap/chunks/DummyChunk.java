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

import java.io.IOException;
import java.io.OutputStream;

/**
 * The purpose of DummyChunk is to mask itself as an another
 * Chunk with the difference that DummyChunks cannot be
 * serialized. It's used to workaround some consistency
 * checks of the Library...
 */
public final class DummyChunk extends AbstractChunk {
    
    private final int type;
    
    /**
     *
     * @param chunk
     */    
    public DummyChunk(AbstractChunk chunk) {
        super(chunk.getContentCode(), chunk.getName());
        
        if (chunk instanceof DummyChunk)
            throw new RuntimeException("DummyChunk cannot be the ");
        
        type = chunk.getType();
    }
    
    /**
     * Returns always 0
     * @return 0
     */    
    public final int getSize() {
        return 0;
    }
    
    /**
     * Returns always 0
     * @return 0
     */    
    public final int getLength() {
        return 0;
    }
    
    /**
     * 
     * @return 0
     */
    public final int getType() {
        return type;
    }
    
    /**
     * DummyChunk doesn't write anything to <tt>out</tt>!!!
     */
    public final void serialize(OutputStream out) throws IOException {
    }
}
