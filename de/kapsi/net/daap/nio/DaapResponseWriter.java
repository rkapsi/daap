/*
 * DaapResponse.java
 *
 * Created on March 31, 2004, 11:51 PM
 */

package de.kapsi.net.daap.nio;


import java.io.IOException;

import java.util.LinkedList;

/**
 *
 * @author  roger
 */
public class DaapResponseWriter {
    
    private LinkedList queue;
    
    /** Creates a new instance of DaapResponseWriter */
    public DaapResponseWriter() {
        queue = new LinkedList();
    }
    
    public void add(DaapResponse response) {
        queue.add(response);
    }
    
    public int size() {
        return queue.size();
    }
    
    public boolean isEmpty() {
        return queue.isEmpty();
    }
    
    private void next() {
        if (!isEmpty())
            queue.removeFirst();
    }
    
    /**
     * Writes <tt>out</tt> to the associated channel and 
     * returns true if <tt>out</tt> was written fully or
     * <tt>false</tt> if some bytes were left in <tt>out</tt>
     */ 
    public boolean write() throws IOException {
        if (!isEmpty()) {
            DaapResponse response = (DaapResponse)queue.getFirst();
            if (response.write()) {
                next();
            } else {
                return false;
            }
        }
        return true;
    }
}
