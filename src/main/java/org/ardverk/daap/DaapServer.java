/*
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004-2010 Roger Kapsi
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

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DaapServer
 * 
 * @author Roger Kapsi
 */
public abstract class DaapServer<T extends DaapConnection> implements Runnable,
        LibraryListener {

    protected static final Logger LOG = LoggerFactory
            .getLogger(DaapServer.class);

    /** The Library */
    protected final Library library;

    /** Queue of Library patches */
    protected final List<Library> libraryQueue = new ArrayList<Library>();

    /** Set of currently active session ids */
    protected final Set<SessionId> sessionIds = new HashSet<SessionId>();

    /** Set of currently active connections */
    protected final List<T> connections = new LinkedList<T>();

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
     * @param streamSource
     *            a DaapStreamSource
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
     * @param filter
     *            a DaapFilter
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
     * Sets the factory for Threads. Servers (NIO) that do not support a
     * Threaded moddel throw an {@see java.lang.UnsupportedOperationException}
     * 
     * @param factory
     *            a DaapThreadFactory
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
        return getDaapConnections().size();
    }

    /**
     * Returns the number of streams
     */
    public synchronized int getNumberOfAudioConnections() {
        return getAudioConnections().size();
    }

    /**
     * Returns the number of pending connections
     */
    public synchronized int getNumberOfPendingConnections() {
        return getPendingConnections().size();
    }

    /**
     * Returns <code>true</code> if sessionId is valid
     */
    protected synchronized boolean isSessionIdValid(SessionId sessionId) {
        return !SessionId.INVALID.equals(sessionId)
                && sessionIds.contains(sessionId);
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
     * Returns <code>true</code> if host with <code>addr</code> is allowed to
     * connect to this DAAP server.
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
    protected synchronized void addPendingConnection(T connection)
            throws IllegalArgumentException {

        if (!connection.isUndef()) {
            throw new IllegalArgumentException();
        }

        connections.add(connection);
    }

    /** Returns an unmodifyable list of pending connections */
    protected synchronized List<T> getPendingConnections() {
        List<T> list = new ArrayList<T>();
        for (T connection : connections) {
            if (connection.isUndef()) {
                list.add(connection);
            }
        }
        return Collections.unmodifiableList(list);
    }

    protected synchronized List<T> getConnections() {
        return Collections.unmodifiableList(new ArrayList<T>(connections));
    }

    /** Returns an unmodifyable list of DAAP connections */
    protected synchronized List<T> getDaapConnections() {
        List<T> list = new ArrayList<T>();
        for (T connection : connections) {
            if (connection.isDaapConnection()) {
                list.add(connection);
            }
        }
        return Collections.unmodifiableList(list);
    }

    /** Returns an unmodifyable List of Audio connections */
    protected synchronized List<T> getAudioConnections() {
        List<T> list = new ArrayList<T>();
        for (T connection : connections) {
            if (connection.isAudioStream()) {
                list.add(connection);
            }
        }
        return Collections.unmodifiableList(list);
    }

    /** Updates connection's state from pending to DAAP or Audio connection */
    protected synchronized boolean updateConnection(T connection) {
        if (connection.isDaapConnection()) {
            return getDaapConnections().size() < config.getMaxConnections();
        } else if (connection.isAudioStream()) {
            return getAudioConnections().size() < config.getMaxConnections();
        }

        return false;
    }

    /** Removes connection */
    protected synchronized void removeConnection(DaapConnection connection)
            throws IllegalStateException {

        if (!connections.remove(connection)) {
            throw new IllegalStateException();
        }
    }

    /** Returns a DAAP connection for the provided sessionId */
    protected synchronized T getDaapConnection(SessionId sessionId) {
        for (T connection : connections) {
            DaapSession session = connection.getSession(false);
            if (session != null && connection.isDaapConnection()
                    && sessionId.equals(session.getSessionId())) {
                return connection;
            }
        }
        return null;
    }

    /** Returns an Audio connection for the provided sessionId */
    protected synchronized T getAudioConnection(SessionId sessionId) {
        for (T connection : connections) {
            DaapSession session = connection.getSession(false);
            if (session != null && connection.isAudioStream()
                    && sessionId.equals(session.getSessionId())) {
                return connection;
            }
        }
        return null;
    }

    /** Clears internal lists */
    protected synchronized void clear() {
        connections.clear();
        sessionIds.clear();
        libraryQueue.clear();
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Name: ").append(config.getServerName()).append("\n");
        buffer.append("Address: ").append(config.getInetSocketAddress())
                .append("\n");
        buffer.append("Backlog: ").append(config.getBacklog()).append("\n");
        buffer.append("Max connections: ").append(config.getMaxConnections())
                .append("\n");
        buffer.append("IsRunning: ").append(isRunning()).append("\n");

        if (isRunning()) {
            buffer.append("Connections: ").append(getNumberOfDaapConnections())
                    .append("\n");
            buffer.append("Streams: ").append(getNumberOfAudioConnections())
                    .append("\n");
        }

        return buffer.toString();
    }
}