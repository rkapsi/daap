
package de.kapsi.net.daap;

import java.io.OutputStream;
import java.io.IOException;

import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

/**
 * A container contains a series of other chunks.
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
    protected void add(Chunk chunk) {
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
