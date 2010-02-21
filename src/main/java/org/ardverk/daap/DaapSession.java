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

package org.ardverk.daap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A session object where session related information can be stored.
 * It's currently a bit overdesigned but we need the features maybe
 * in the future.
 *
 * @author  Roger Kapsi
 */
public class DaapSession {
    
    private final long creationTime = System.currentTimeMillis();
    private final Map<String, Object> attributes = new HashMap<String, Object>();
    
    private final SessionId sessionId;
    private boolean valid;
    private long lastAccesedTime;
    private int maxInactiveTime;
    
    /**
     * Creates a new DaapSession
     * @param sessionId
     */    
    public DaapSession(SessionId sessionId) {
        this.sessionId = sessionId;
        this.valid = true;
        this.lastAccesedTime = creationTime;
        this.maxInactiveTime = 1800;
    }
    
    /**
     * Returns the unique session id
     * 
     * @return the sessionId of this DaapSession
     */    
    public SessionId getSessionId() {
        return sessionId;
    }
    
    /**
     * Returns the time when this session was accessed last
     * time. <p>NOTE: currently unused!</p>
     * 
     * @return
     */    
    public synchronized long getLastAccessedTime() {
        return lastAccesedTime;
    }
    
    /**
     * Returns the creation time of this Session.
     * 
     * @return creation time
     */    
    public long getCreationTime() {
        return creationTime;
    }
    
    /**
     * Returns the maximum inactive time.
     * <p>NOTE: currently unused!</p>
     * 
     * @return maximum inactive time
     */    
    public synchronized int getMaxInactiveTime() {
        return maxInactiveTime;
    }
    
    /**
     * Sets the maximum inactive time. 
     * <p>NOTE: currently unused!</p>
     * 
     * @param maxInactiveTime the maximum inactive time
     */    
    public synchronized void setMaxInactiveTime(int maxInactiveTime) {
        this.maxInactiveTime = maxInactiveTime;
    }
    
    /**
     * Retruns <code>true</code> if this session object
     * is valid and <code>false</code> otherwise.
     * 
     * @return <code>true</code> if sesssion is valid
     */    
    public synchronized boolean isValid() {
        return valid;
    }
    
    /**
     * Invalidates this session object
     */
    public synchronized void invalidate() {
        valid = false;
        attributes.clear();
    }
    
    /**
     * Returns an object that is associated with the key
     * or <code>null</code> if nothing is associated with
     * the key
     * 
     * @param key a key
     * @return an object that is associated with the key or 
     *      <code>null</code>
     */    
    public synchronized Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    /**
     * Attachs key/object to this Session
     * 
     * @param key a key
     * @param value an arbitrary object
     * @return <code>null</code> or the previous object that was
     *  associated with the key
     */    
    public synchronized Object setAttribute(String key, Object value) {
        return attributes.put(key, value);
    }
    
    /**
     * Removes and returns an attribute that is associated with
     * the key or <code>null</code> if key is unknown.
     * 
     * @param key a key
     * @return the associated object or <code>null</code>
     */    
    public synchronized Object removeAttribute(String key) {
        return attributes.remove(key);
    }
    
    /**
     * Returns an Iterator of all keys
     * 
     * @return an Iterator
     */    
    public synchronized Iterator<String> getAttributeNames() {
        return attributes.keySet().iterator();
    }
    
    /**
     * Returs <code>true</code> if this Session object has a attribute
     * associated with the key
     * 
     * @param key a key
     * @return <code>true</code> if this Session has a such attribute
     */    
    public synchronized boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }
    
    /**
     * Sets lastAccesedTime to 'now'
     *
     */
    synchronized void update() {
        lastAccesedTime = System.currentTimeMillis();
    }
      
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
