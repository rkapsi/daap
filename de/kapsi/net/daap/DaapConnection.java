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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An abstract base class for DaapConnections.
 *
 * @author  Roger Kapsi
 */
public abstract class DaapConnection {
    
    private static final Log LOG = LogFactory.getLog(DaapConnection.class);
    
    public static final int TIMEOUT = 3000;
    public static final int LIBRARY_TIMEOUT = 30000;
    
    /** Undef type of connection */
    protected static final int UNDEF  = DaapUtil.NULL;
    
    /** A DAAP connection */
    protected static final int DAAP = 1;
    
    /** An audio stream */
    protected static final int AUDIO  = 2;
    
    protected final DaapResponseWriter writer;
    
    protected DaapServer server;
    protected DaapSession session;
    
    protected int type = UNDEF;
    protected int protocolVersion = UNDEF;
    protected String nonce;
    
    protected LinkedList libraryQueue;
    
    protected boolean locked = false;
    
    public DaapConnection(DaapServer server) {
        this.server = server;
        writer = new DaapResponseWriter();
        libraryQueue = new LinkedList();
    }
    
    /**
     * Call this to initiate an update which causes the 
     * client to update to the current Library revision.
     *
     * @throws IOException
     */    
    public abstract void update() throws IOException;
    
    /**
     * Writes data from the queue to the Channel/OutputStream
     * 
     * @throws IOException
     * @return true if keepAlive
     */ 
    public boolean write() throws IOException {
        
        if (writer.write()) {

            if (isAudioStream()) {
                return false;
            }
        }

        return true;
    }
    
    /**
     * Called when a DaapConnection is beeing removed
     * from the connection pool.
     */
    public void close() {
        
        writer.clear();
        
        if (session != null) {
            session.invalidate();
            session = null;
        }
    }
    
    /**
     * Returns the DaapServer to which this DaapConnection is
     * associated to.
     *
     * @return the associated DaapServer
     */    
    public DaapServer getServer() {
        return server;
    }
    
    /**
     * Sets the type of this connection. Either UNDEF, DAAP
     * or an AUDIO stream
     * 
     * @param type the type of this connection
     */
    protected void setConnectionType(int type) {
        this.type = type;
    }
    
    /**
     * Sets the protocol version of this connection
     */
    protected void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    
    /**
     * Returns <code>true</code> if this connection is an audio
     * stream.
     *
     * @return true if this is an audio stream
     */ 
    public boolean isAudioStream() {
        return (type==DaapConnection.AUDIO);
    }
    
    /**
     * Returns <code>true</code> if this connection is a DAAP
     * connection (handles Requests/Respones).
     *
     * @return true if this is a DAAP connection 
     */    
    public boolean isDaapConnection() {
        return (type==DaapConnection.DAAP);
    }
    
    /**
     * Returns <code>true</code> if the type of this connection
     * is currently indetermined.
     * 
     * @return <code>true</code> if connection is indetermined
     */    
    public boolean isUndef() {
        return (type==DaapConnection.UNDEF);
    }
    
    /**
     * Returns the protocol version of this connection or
     * DaapUtil.UNDEF_VALUE if it is currently unknown
     * 
     * @return protocol version of this connection
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }
    
    /**
     * Creates if nessesary a new DaapSession and returns it.
     *
     * @param create if true creates a new Session object if
     *  necessary
     * @return a DaapSession object or null if create is false
     *  and no DaapSession object existed
     */    
    public DaapSession getSession(boolean create) {
        if (session == null && create) {
            session = new DaapSession(server.createSessionId());
        }
        
        return session;
    }
    
    /**
     * 
     */
    public SessionId getSessionId(boolean create) {
        SessionId sessionId = SessionId.INVALID;
        DaapSession session = getSession(create);
        if (session != null) {
            sessionId = session.getSessionId();
        }
        return sessionId;
    }
    
    /**
     * Clears the library queue
     */
    public void clearLibraryQueue() {
        synchronized(libraryQueue) {
            libraryQueue.clear();
        }
    }
    
    /**
     * Adds library to the library queue
     */
    public void enqueueLibrary(Library library) {
        synchronized(libraryQueue) {
            libraryQueue.add(library);
        }
    }
    
    /**
     * Returns the first item from the library queue
     */
    protected Library getFirstInQueue() {
        synchronized(libraryQueue) {
            if (!libraryQueue.isEmpty()) {
                return (Library)libraryQueue.getFirst();
            }
            return server.getLibrary();
        }
    }
    
    /**
     * Returns the last item from the library queue
     */
    protected Library getLastInQueue() {
        synchronized(libraryQueue) {
            if (!libraryQueue.isEmpty()) {
                return (Library)libraryQueue.getLast();
            }
            return server.getLibrary();
        }
    }
    
    /**
     * Returns the next item from the library queue
     */
    public Library nextLibrary(DaapRequest request) {
        synchronized(libraryQueue) {
            int delta = request.getDelta();
            if (delta == DaapUtil.NULL
                    || libraryQueue.isEmpty()) {
                return server.getLibrary();
            }
            
            Library first = (Library)libraryQueue.getFirst();
            
            if (first.getRevision() != request.getRevisionNumber()) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Client doesn't request the current revision: " + delta + "/" + request.getRevisionNumber() + "/" + first.getRevision());
                }
                clearLibraryQueue();
                return null;
            } else if (delta > first.getRevision()) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Client is ahead of us: " + delta + "/" + first.getRevision());
                }
                clearLibraryQueue();
                return null;
            } else if (first.getRevision() == delta) {
                libraryQueue.removeFirst();
                
                if (libraryQueue.isEmpty()) {
                    return server.getLibrary();
                } else {
                    
                    if (request.getRevisionNumber() != delta) {
                        return nextLibrary(request);
                    } else {
                        return nextLibrary(new DaapRequest(this, getSessionId(false), 
                                request.getRevisionNumber()+1, delta));
                    }
                }
            } else {
                return first;
            }
        }
    }
    
    /**
     * Locks the connection
     * @see #update()
     * @see DaapRequestProcessor#processUpdateRequest(DaapRequest)
     */
    protected synchronized void lock() {
        locked = true;
    }
    
    /**
     * Unlocks the connection
     * @see #update()
     * @see DaapRequestProcessor#processUpdateRequest(DaapRequest)
     */
    protected synchronized void unlock() {
        locked = false;
    }
    
    /**
     * Returns true if connection is marked as locked
     * @see #update()
     * @see DaapRequestProcessor#processUpdateRequest(DaapRequest)
     */
    protected synchronized boolean isLocked() {
        return locked;
    }
    
    /**
     * 
     */
    synchronized String getNonce() {
        return nonce;
    }

    /**
     * 
     */
    synchronized String createNonce() {
        nonce = DaapUtil.nonce();
        return nonce;
    }
}
