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

import java.util.HashMap;
import java.util.Iterator;

/**
 * A session object where session related information can be stored.
 * It's currently a bit overdesigned but we need the features maybe
 * in the future.
 *
 * @author  Roger Kapsi
 */
public class DaapSession {
    
    private final long creationTime = System.currentTimeMillis();
    private final HashMap attributes = new HashMap();
    
    private final Integer sessionId;
    private boolean valid;
    private long lastAccesedTime;
    private int maxInactiveTime;
    
    /**
     * Creates a new DaapSession
     * @param sessionId
     */    
    public DaapSession(Integer sessionId) {
        this.sessionId = sessionId;
        this.valid = true;
        this.lastAccesedTime = creationTime;
        this.maxInactiveTime = 1800;
    }
    
    /**
     *
     * @return the sessionId of this DaapSession
     */    
    public Integer getSessionId() {
        return sessionId;
    }
    
    /**
     *
     * @return
     */    
    public synchronized long getLastAccessedTime() {
        return lastAccesedTime;
    }
    
    /**
     *
     * @return
     */    
    public long getCreationTime() {
        return creationTime;
    }
    
    /**
     *
     * @return
     */    
    public synchronized int getMaxInactiveTime() {
        return maxInactiveTime;
    }
    
    /**
     *
     * @param maxInactiveTime
     */    
    public synchronized void setMaxInactiveTime(int maxInactiveTime) {
        this.maxInactiveTime = maxInactiveTime;
    }
    
    /**
     *
     * @return
     */    
    public synchronized boolean isValid() {
        return valid;
    }
    
    public synchronized void invalidate() {
        valid = false;
        attributes.clear();
    }
    
    /**
     *
     * @param key
     * @return
     */    
    public synchronized Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    /**
     *
     * @param key
     * @param value
     * @return
     */    
    public synchronized Object setAttribute(String key, Object value) {
        return attributes.put(key, value);
    }
    
    /**
     *
     * @param key
     * @return
     */    
    public synchronized Object removeAttribute(String key) {
        return attributes.remove(key);
    }
    
    /**
     *
     * @return
     */    
    public synchronized Iterator getAttributeNames() {
        return attributes.keySet().iterator();
    }
    
    /**
     *
     * @param key
     * @return
     */    
    public synchronized boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }
    
    synchronized void update() {
        lastAccesedTime = System.currentTimeMillis();
    }
    
    /**
     *
     * @return
     */    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append("class: ").append(getClass().getName()).append("\n");
        buffer.append(" sessionId: ").append(sessionId).append("\n");
        buffer.append(" maxInactiveTime: ").append(maxInactiveTime).append("\n");
        buffer.append(" creationTime: ").append(creationTime).append("\n");
        buffer.append(" lastAccesedTime: ").append(lastAccesedTime).append("\n");
        buffer.append(" valid: ").append(valid).append("\n");
        buffer.append(" attributes: ").append(attributes).append("\n");
        
        return buffer.toString();
    }
}
