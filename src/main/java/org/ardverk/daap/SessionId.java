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
     * Creates a SessionId from sessionId. It returns {@link #INVALID} if
     * sessionId is nagative or zero!
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
     * @param uniqueSet
     *            A set of SessionIds that are already taken
     */
    public static SessionId createSessionId(Set<? extends SessionId> uniqueSet) {

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
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

    @Override
    public int hashCode() {
        return sessionId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SessionId)) {
            return false;
        }

        return ((SessionId) o).sessionId == sessionId;
    }

    @Override
    public String toString() {
        return Integer.toString(sessionId);
    }
}
