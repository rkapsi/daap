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

import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

/**
 * A container contains a series of other chunks.
 *
 * @author  Roger Kapsi
 */
public class ContainerChunk extends AbstractChunk {
    
    private Collection collection;
    
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
        collection.add(chunk);
    }
    
    // Only required for testing etc.
        /*public boolean remove(Chunk chunk) {
                return collection.remove(chunk);
        }
         
        public boolean contains(Chunk chunk) {
                return collection.contains(chunk);
        }
         
        public Iterator iterator() {
                return collection.iterator();
        }
         
        public void clear() {
                collection.clear();
        }*/
    
    /**
     * Returns the number of childs
     */
    public int size() {
        return collection.size();
    }
    
    /**
     * Returns the length of this chunk <tt>sum(child[i].getSize())</tt>
     */
    public int getLength() {
        int length = 0;
        
        Iterator it = collection.iterator();
        
        while(it.hasNext()) {
            length += ((Chunk)it.next()).getSize();
        }
        
        return length;
    }
    
    /**
     * Returns <tt>Chunk.CONTAINER_TYPE</tt>
     */
    public int getType() {
        return Chunk.CONTAINER_TYPE;
    }
    
    public void serialize(OutputStream os) throws IOException {
        super.serialize(os);
        
        Iterator it = collection.iterator();
        
        while(it.hasNext()) {
            ((Chunk)it.next()).serialize(os);
        }
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(super.toString()).append("=[");
        Iterator it = collection.iterator();
        while(it.hasNext()) {
            buffer.append(it.next());
            if (it.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append("]");
        return buffer.toString();
    }
}
