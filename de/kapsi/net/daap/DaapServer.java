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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DaapServer
 *
 * @author  Roger Kapsi
 */
public abstract class DaapServer implements Runnable, LibraryListener {
    
    protected static final Log LOG = LogFactory.getLog(DaapServer.class);
    
    /** The Library */
    protected final Library library;
    
    /** Queue of Library patches */
    protected final List<Library> libraryQueue = new ArrayList<Library>();
    
    /** Set of currently active session ids */
    protected final Set<SessionId> sessionIds = new HashSet<SessionId>();
    
    /** List of pending connections */
    protected final LinkedList pending = new LinkedList();
    
    /** List of DAAP connections */
    protected final LinkedList connections = new LinkedList();
    
    /** List of Audio connections */
    protected final LinkedList streams = new LinkedList();
    
    /** A DaapConfig instance */
    protected DaapConfig config;
    
    /** A DaapFilter instance */
    protected DaapFilter filter;
    
    /** Source for Audio streams */
    protected DaapStreamSource streamSource;
    
    /** The Authenticator */
    protected DaapAuthenticator authenticator;
    
    /** Flag for wheather or not the server is running */
    protected boolean running = false;
    
    public DaapServer(Library library, DaapConfig config) {
        this.library = library;
        this.config = config;
        library.addLibraryListener(this);
    }
    
    /**
     * Returns the Library of this server
     * 
     * @return Library
     */  
    public Library getLibrary() {
        return library;
    }

    public synchronized void libraryChanged(Library library, Library branch) {
        if (isRunning() && getNumberOfDaapConnections() > 0) {
            libraryQueue.add(branch);
            update();
        }
    }    
    
    /**
     * Returns the DaapConfig of this server
     * 
     * @return DaapConfig of this server
     */    
    public DaapConfig getConfig() {
        return config;
    }
    
    /**
     * Sets the DaapStreamSource for this server
     * 
     * @param streamSource a DaapStreamSource
     */  
    public synchronized void setStreamSource(DaapStreamSource streamSource) {
        this.streamSource = streamSource;
    }
    
    
    /**
     * Retrieves the DaapStreamSource of this server
     * 
     * @return DaapStreamSource or <code>null</code>
     */  
    public synchronized DaapStreamSource getStreamSource() {
        return streamSource;
    }
    
    /**
     * Sets a DaapFilter for this server
     * 
     * @param filter a DaapFilter
     */  
    public synchronized void setFilter(DaapFilter filter) {
        this.filter = filter;
    }
    
    /**
     * Returns a DaapFilter
     * 
     * @return a DaapFilter or <code>null</code>
     */   
    public synchronized DaapFilter getFilter() {
        return filter;
    }
    
    public synchronized void setAuthenticator(DaapAuthenticator authenticator) {
        this.authenticator = authenticator;
    }
    
    /**
     * Returns the DaapAuthenticator from Library
     */
    public synchronized DaapAuthenticator getAuthenticator() {
        return authenticator;
    }
    
    /**
     * Sets the factory for Threads. Servers (NIO) that do not 
     * support a Threaded moddel throw an 
     * {@see java.lang.UnsupportedOperationException}
     * 
     * @param factory a DaapThreadFactory
     */
    public synchronized void setThreadFactory(DaapThreadFactory factory) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Binds this server to the SocketAddress supplied by DaapConfig
     * 
     * @throws IOException
     */ 
    public abstract void bind() throws IOException;
    
    /**
     * Returns <code>true</code> if the server is running.
     * 
     * @return <code>true</code> if the server is running
     */
    public synchronized boolean isRunning() {
        return running;
    }
    
    /**
     * Stops the DAAP Server
     */
    public abstract void stop();
    
    /**
     * Disconnects all from the server
     */
    public abstract void disconnectAll();
        
    /**
     * Call this to notify the server that Library has changed
     */
    protected abstract void update();
    
    /**
     * Returns the number of connections
     */
    public synchronized int getNumberOfDaapConnections() {
        return connections.size();
    }
    
    /**
     * Returns the number of streams
     */
    public synchronized int getNumberOfAudioConnections() {
        return streams.size();
    }
    
    /**
     * Returns the number of pending connections
     */
    public synchronized int getNumberOfPendingConnections() {
        return pending.size();
    }
    
    /**
     * Returns <code>true</code> if sessionId is valid
     */
    protected synchronized boolean isSessionIdValid(SessionId sessionId) {
        return !SessionId.INVALID.equals(sessionId) && sessionIds.contains(sessionId);
    }
    
    /**
     * Creates and returns an unique session-id
     */
    protected synchronized SessionId createSessionId() {
        SessionId sid = SessionId.createSessionId(sessionIds);
        sessionIds.add(sid);
        return sid;
    }
    
    protected synchronized void destroySessionId(SessionId sessionId) {
        sessionIds.remove(sessionId);
    }
    
    /**
     * Returns <code>true</code> if host with <code>addr</code> is
     * allowed to connect to this DAAP server.
     * 
     * @return true host with <code>addr</code> is allowed to connect
     */
    protected synchronized boolean accept(InetAddress addr) {
        if (filter != null && filter.accept(addr) == false) {
            
            if (LOG.isInfoEnabled()) {
                LOG.info("DaapFilter refused connection from " + addr);
            }
            
            return false;
        }
        
        return true;
    }
    
    /** Adds connection to pending connections pool */
    protected synchronized void addPendingConnection(DaapConnection connection) 
            throws IllegalArgumentException {
        
        if (!connection.isUndef()) {
            throw new IllegalArgumentException();
        }
        
        pending.add(connection);
    }
    
    /** Returns an unmodifyable list of pending connections */
    protected synchronized List getPendingConnections() {
        List l = Collections.EMPTY_LIST;
        if (!pending.isEmpty()) {
            l = Collections.unmodifiableList(new ArrayList(pending));
        }
        return l;
    }
    
    /** Adds connection to DAAP connection pool */
    protected synchronized boolean addDaapConnection(DaapConnection connection) 
            throws IllegalStateException, IllegalArgumentException {
        
        if (!pending.remove(connection)) {
            throw new IllegalStateException();
        }
        
        if (!connection.isDaapConnection()) {
            throw new IllegalArgumentException();
        }
        
        if (connections.size() < config.getMaxConnections()) {
            connections.add(connection);
            //connection.addLibrary(library);
            return true;
        } else {
            return false;
        }
    }
    
    /** Returns an unmodifyable list of DAAP connections */
    protected synchronized List getDaapConnections() {
        List l = Collections.EMPTY_LIST;
        if (!connections.isEmpty()) {
            l = Collections.unmodifiableList(new ArrayList(connections));
        }
        return l;
    }

    /** Adds connection to audio connection pool */
    protected synchronized boolean addAudioConnection(DaapConnection connection) 
            throws IllegalStateException, IllegalArgumentException {
        
        if (!pending.remove(connection)) {
            throw new IllegalStateException();
        }
        
        if (!connection.isAudioStream()) {
            throw new IllegalArgumentException();
        }
        
        if (streams.size() < config.getMaxConnections()) {
            streams.add(connection);
            return true;
        } else {
            return false;
        }
    }
    
    /** Returns an unmodifyable List of Audio connections */
    protected synchronized List getAudioConnections() {
        List l = Collections.EMPTY_LIST;
        if (!streams.isEmpty()) {
            l = Collections.unmodifiableList(new ArrayList(streams));
        }
        return l;
    }

    /** Updates connection's state from pending to DAAP or Audio connection */
    protected synchronized boolean updateConnection(DaapConnection connection) {
        if (connection.isDaapConnection()) {
            return addDaapConnection(connection);
        } else if (connection.isAudioStream()) {
            return addAudioConnection(connection);
        } else {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unknown state of connection: " + connection);
            }
        }
        
        return false;
    }
    
    /** Removes connection */
    protected synchronized void removeConnection(DaapConnection connection) 
            throws IllegalStateException {
        
        if (connection.isUndef()) {
            if (!pending.remove(connection)) {
                throw new IllegalStateException();
            }
        } else if (connection.isDaapConnection()) {
            if (!connections.remove(connection)) {
                throw new IllegalStateException();
            }
        } else if (connection.isAudioStream()) {
            if (!streams.remove(connection)) {
                throw new IllegalStateException();
            }
        } else {
            throw new IllegalStateException();
        }
    }
    
    /** Returns a DAAP connection for the provided sessionId */
    protected synchronized DaapConnection getDaapConnection(SessionId sessionId) {
        for(Iterator it = connections.iterator(); it.hasNext(); ) {
            DaapConnection connection = (DaapConnection)it.next();
            DaapSession session = connection.getSession(false);
            if (session != null && sessionId.equals(session.getSessionId())) {
                return connection;
            }
        }
        return null;
    }
    
    /** Returns an Audio connection for the provided sessionId */
    protected synchronized DaapConnection getAudioConnection(SessionId sessionId) {
        for(Iterator it = streams.iterator(); it.hasNext(); ) {
            DaapConnection connection = (DaapConnection)it.next();
            DaapSession session = connection.getSession(false);
            if (session != null && sessionId.equals(session.getSessionId())) {
                return connection;
            }
        }
        return null;
    }
    
    /** Clears internal lists */
    protected synchronized void clear() {
        pending.clear();
        connections.clear();
        streams.clear();
        sessionIds.clear();
        libraryQueue.clear();
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Name: ").append(config.getServerName()).append("\n");
        buffer.append("Address: ").append(config.getInetSocketAddress()).append("\n");
        buffer.append("Backlog: ").append(config.getBacklog()).append("\n");
        buffer.append("Max connections: ").append(config.getMaxConnections()).append("\n");
        buffer.append("IsRunning: ").append(isRunning()).append("\n");
        
        if (isRunning()) {
            buffer.append("Connections: ").append(getNumberOfDaapConnections()).append("\n");
            buffer.append("Streams: ").append(getNumberOfAudioConnections()).append("\n");
        }
        
        return buffer.toString();
    }
}
