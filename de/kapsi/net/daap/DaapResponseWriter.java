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

package de.kapsi.net.daap;

import java.io.IOException;
import java.util.LinkedList;

/**
 * FIFO based queue to write DaapResponses.
 *
 * @author  Roger Kapsi
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
    
    /**
     * Returns the number of unsend DaapRespones in the
     * queue.
     */
    public int size() {
        return queue.size();
    }
    
    /**
     * Clears the queue (bad idea)
     */
    public void clear() {
        queue.clear();
    }
    
    /**
     * Returns <tt>true</tt> if queue is empty.
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }
    
    /**
     * Send the next element
     */
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
