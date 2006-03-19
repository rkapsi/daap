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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * A container contains a series of other chunks.
 *
 * @author  Roger Kapsi
 */
public class ContainerChunk extends AbstractChunk {
    
    protected Collection collection;
    
    /**
     * Creates a new ContainerChunk with an <tt>ArrayList</tt>
     * as the underlying collection.
     */
    protected ContainerChunk(String type, String name) {
        this(type, name, new ArrayList());
    }
   
    /**
     * Note: you should use always a List as the underlying
     * collection.
     */
    protected ContainerChunk(String type, String name, Collection collection) {
        super(type, name);
        this.collection = collection;
    }
    
    /**
     * Adds <tt>chunk</tt> to this container
     */
    public void add(Chunk chunk) {
        if (chunk == null) {
            throw new IllegalArgumentException();
        }
        collection.add(chunk);
    }
         
    public Iterator iterator() {
        return Collections.unmodifiableCollection(collection).iterator();
    }
    
    /**
     * Returns the number of childs
     */
    public int size() {
        return collection.size();
    }
    
    /**
     * Returns {@see Chunk.CONTAINER_TYPE}
     */
    public int getType() {
        return Chunk.CONTAINER_TYPE;
    }

    public String toString(int indent) {
        StringBuffer buffer = new StringBuffer(indent(indent));
        buffer.append(name).append("(").append(getContentCodeString()).append("; container)\n");
        
        Iterator it = iterator();
        for(int i = 0; it.hasNext(); i++) {
            AbstractChunk chunk = (AbstractChunk)it.next();
            buffer./*append(i).append(": ").*/append(chunk.toString(indent + 4));
            if (it.hasNext()) {
                buffer.append("\n");
            }
        }
        return buffer.toString();
    }
}
