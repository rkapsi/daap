/* 
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2005 Roger Kapsi, info at kapsi dot de
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

import java.util.Set;

/**
 * A wrapper class for SessionIds
 */
public final class SessionId {
    
    /** An invalid SessionId. Use it to initialize SessionIds etc. */
    public static final SessionId INVALID = new SessionId(DaapUtil.NULL);
    
    /** the session id */
    private final int sessionId;
    
    /** Use factory methods to construct SessionIds! */
    private SessionId(int sessionId) {
        this.sessionId = sessionId;
    }
    
    /** 
     * Creates a SessionId from sessionId. It returns {@link #INVALID} 
     * if sessionId is nagative or zero!
     */
    public static SessionId createSessionId(int sessionId) {
        if (sessionId <= DaapUtil.NULL) {
            return INVALID;
        }
        
        return new SessionId(sessionId);
    }
    
    /**
     * Creates and returns a new random SessionId
     * 
     * @param uniqueSet A set of SessionIds that are already taken
     */
    public static SessionId createSessionId(Set uniqueSet) {

        for(int i = 0; i < Integer.MAX_VALUE; i++) {
            int id = DaapUtil.nextInt(Integer.MAX_VALUE);
            if (id != DaapUtil.NULL) {
                SessionId sessionId = new SessionId(id);
                if (uniqueSet == null || !uniqueSet.contains(sessionId)) {
                    return sessionId;
                }
            }
        }
        
        throw new IndexOutOfBoundsException("All 2^31-1 IDs are in use");
    }
    
    /**
     * Parses and returns a SessionId
     */
    public static SessionId parseSessionId(String s) 
            throws NumberFormatException {
        return createSessionId(Integer.parseInt(s));
    }
    
    /**
     * Returns the SessionId as int value
     */
    public int intValue() {
        return sessionId;
    }
    
    public int hashCode() {
        return sessionId;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof SessionId)) {
            return false;
        }
        
        return ((SessionId)o).sessionId == sessionId;
    }
    
    public String toString() {
        return Integer.toString(sessionId);
    }
}
