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
    
    private LinkedList<DaapResponse> queue;
    
    /** Creates a new instance of DaapResponseWriter */
    public DaapResponseWriter() {
        queue = new LinkedList<DaapResponse>();
    }
    
    /**
     * Adds <code>response</code> to the FIFO queue
     * 
     * @param response the object to be written
     */
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
     * Returns <code>true</code> if queue is empty and
     * there is nothing more to send
     * 
     * @return <code>true</code> if queue is empty
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }
    
    /**
     * Send the next element
     */
    private void next() {
        if (!isEmpty()) {
            queue.removeFirst();
        }
    }
    
    /**
     * Writes DaapResponses from the internal queue to out 
     * 
     * @return <code>true</code> if everything was fully written and
     *  the queue is empty.
     * @throws IOException
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
