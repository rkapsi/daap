
package de.kapsi.net.daap;

import java.util.HashMap;
import java.util.Iterator;

/**
 * A session object where session related information can be stored.
 * It's currently a bit overdesigned but we need the features maybe
 * in the future.
 */
public class DaapSession {
	
	private final long creationTime = System.currentTimeMillis();
	private final HashMap attributes = new HashMap();
	
	private Integer sessionId;
	private boolean valid;
	private long lastAccesedTime;
	private int maxInactiveTime;
	
	public DaapSession(Integer sessionId) {
		this.sessionId = sessionId;
		this.valid = true;
		this.lastAccesedTime = creationTime;
		this.maxInactiveTime = 1800;
	}
	
	public Integer getSessionId() {
		return sessionId;
	}
	
	public long getLastAccessedTime() {
		return lastAccesedTime;
	}
	
	public long getCreationTime() {
		return creationTime;
	}
	
	public int getMaxInactiveTime() {
		return maxInactiveTime;
	}
	
	public void setMaxInactiveTime(int maxInactiveTime) {
		this.maxInactiveTime = maxInactiveTime;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public void invalidate() {
		valid = false;
		attributes.clear();
	}
	
	public Object getAttribute(String key) {
		return attributes.get(key);
	}
	
	public Object addAttribute(String key, Object value) {
		return attributes.put(key, value);
	}
	
	public Object removeAttribute(String key) {
		return attributes.remove(key);
	}
	
	public Iterator getAttributeNames() {
		return attributes.keySet().iterator();
	}
	
	public boolean hasAttribute(String key) {
		return attributes.containsKey(key);
	}
	
	void update() {
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
