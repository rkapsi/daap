/*
 * Digital Audio Access Protocol (DAAP) Library
 * Copyright (C) 2004-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.daap;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * FIFO based queue to write DaapResponses.
 * 
 * @author Roger Kapsi
 */
public class DaapResponseWriter {

    private List<DaapResponse> queue;

    /** Creates a new instance of DaapResponseWriter */
    public DaapResponseWriter() {
        queue = new LinkedList<DaapResponse>();
    }

    /**
     * Adds <code>response</code> to the FIFO queue
     * 
     * @param response
     *            the object to be written
     */
    public void add(DaapResponse response) {
        queue.add(response);
    }

    /**
     * Returns the number of unsend DaapRespones in the queue.
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
     * Returns <code>true</code> if queue is empty and there is nothing more to
     * send
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
            queue.remove(0);
        }
    }

    /**
     * Writes DaapResponses from the internal queue to out
     * 
     * @return <code>true</code> if everything was fully written and the queue
     *         is empty.
     * @throws IOException
     */
    public boolean write() throws IOException {
        if (!isEmpty()) {
            DaapResponse response = queue.get(0);
            if (response.write()) {
                next();
            } else {
                return false;
            }
        }
        return true;
    }
}
