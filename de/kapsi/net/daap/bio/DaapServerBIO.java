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

package de.kapsi.net.daap.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.DaapAuthenticator;
import de.kapsi.net.daap.DaapConfig;
import de.kapsi.net.daap.DaapConnection;
import de.kapsi.net.daap.DaapFilter;
import de.kapsi.net.daap.DaapServer;
import de.kapsi.net.daap.DaapSession;
import de.kapsi.net.daap.DaapStreamSource;
import de.kapsi.net.daap.DaapThreadFactory;
import de.kapsi.net.daap.DaapUtil;
import de.kapsi.net.daap.Library;
import de.kapsi.net.daap.SimpleConfig;

/**
 * This DAAP server is written with the classical I/O and multible Threads.
 *
 * @author  Roger Kapsi
 */
public class DaapServerBIO implements DaapServer {
    
    private static final Log LOG = LogFactory.getLog(DaapServerBIO.class);
   
    private int threadNo = 0;
    
    private Library library;
    
    private DaapFilter filter;
    
    private HashSet sessionIds;
    private HashSet connections;
    private HashSet streams;
    
    private DaapConfig config;
    private DaapAuthenticator authenticator;
    private DaapStreamSource streamSource;
    private DaapThreadFactory threadFactory;
    
    private ServerSocket ssocket;
    
    private boolean running = false;
    
    /**
     * Creates a new DAAP server with Library and {@see SimpleConfig}
     * 
     * @param library a Library
     */  
    public DaapServerBIO(Library library) {
        this(library, new SimpleConfig());
    }
    
    /**
     * Creates new DAAP server with Library, a {@see SimpleConfig} and 
     * the Port
     * 
     * @param library a Library
     * @param port a Port used by SimpleConfig
     */  
    public DaapServerBIO(Library library, int port) {
        this(library, new SimpleConfig(port));
    }
    
    /**
     * Creates a new DAAP server with Library and DaapConfig
     * 
     * @param library a Library
     * @param config a DaapConfig
     */  
    public DaapServerBIO(Library library, DaapConfig config) {
        
        this.library = library;
        this.config = config;
        
        threadFactory = new DaapThreadFactoryImpl();
        
        sessionIds = new HashSet();
        connections = new HashSet();
        streams = new HashSet();
    }
    
    /**
     * Returns the Library of this server
     * 
     * @return Library
     */  
    public Library getLibrary() {
        return library;
    }
    
    /**
     * Sets the DaapConfig for this server
     * 
     * @param config DaapConfig
     */
    public void setConfig(DaapConfig config) {
        this.config = config;
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
     * Sets the DaapAuthenticator for this server
     * 
     * @param authenticator a DaapAuthenticator
     */ 
    public void setAuthenticator(DaapAuthenticator authenticator) {
        this.authenticator = authenticator;
    }
    
    /**
     * Retrieves the DaapAuthenticator of this server
     * 
     * @return DaapAuthenticator or <code>null</code>
     */ 
    public DaapAuthenticator getAuthenticator() {
        return authenticator;
    }
    
    /**
     * Sets the DaapStreamSource for this server
     * 
     * @param streamSource a DaapStreamSource
     */
    public void setStreamSource(DaapStreamSource streamSource) {
        this.streamSource = streamSource;
    }
    
    /**
     * Retrieves the DaapStreamSource of this server
     * 
     * @return DaapStreamSource or <code>null</code>
     */
    public DaapStreamSource getStreamSource() {
        return streamSource;
    }
    
    /**
     * Sets a DaapFilter for this server
     * 
     * @param filter a DaapFilter
     */ 
    public void setFilter(DaapFilter filter) {
        this.filter = filter;
    }
    
    /**
     * Returns a DaapFilter
     * 
     * @return a DaapFilter or <code>null</code>
     */
    public DaapFilter getFilter() {
        return filter;
    }
    
    /**
     * Sets the DaapThreadFactory for this DAAP server
     * 
     * @param fectory a DaapThreadFactory
     */
    public void setThreadFactory(DaapThreadFactory factory) {
        if (factory == null) {
            threadFactory = new DaapThreadFactoryImpl();
        } else {
            threadFactory = factory;
        }
    }
    
    /**
     * Binds this server to the SocketAddress supplied by DaapConfig
     * 
     * @throws IOException
     */
    public synchronized void bind() throws IOException {
        if (running)
            return;
        
        SocketAddress bindAddr = config.getInetSocketAddress();
        int backlog = config.getBacklog();
        
        ssocket = new ServerSocket();
        ssocket.bind(bindAddr, backlog);
        
        if (LOG.isInfoEnabled()) {
            LOG.info("DaapServerBIO bound to " + bindAddr);
        }
    }
    
    /**
     * Returns <code>true</code> if DAAP Server
     * accepts incoming connections.
     */
    public synchronized boolean isRunning() {
        return running;
    }
    
    /**
     * Stops the DAAP Server
     */
    public synchronized void stop() {
        
        if (!running)
            return;
            
        running = false;
        
        try {
            if (ssocket != null)
                ssocket.close();
        } catch (IOException err) {
            LOG.error(err);
        }
        
        disconnectAll();
    }
    
    /**
     * Disconnects all DAAP and Stream connections
     */
    public synchronized void disconnectAll() {
        
        synchronized(connections) {
            Iterator it = connections.iterator();
            while(it.hasNext()) {
                ((DaapConnectionBIO)it.next()).disconnect();
            }
            connections.clear();
        }
        
        synchronized(streams) {
            Iterator it = streams.iterator();
            while(it.hasNext()) {
                ((DaapConnectionBIO)it.next()).disconnect();
            }
            streams.clear();
        }
        
        synchronized(sessionIds) {
            sessionIds.clear();
        }
    }
    
    /**
     * Adds connection to the internal connection pool and returns true
     * on success. False is retuned in the following cases: Max connections
     * reached or server is down.
     */
    synchronized boolean addConnection(DaapConnectionBIO connection) {
        
        if (!isRunning()) {
            
            if (LOG.isInfoEnabled()) {
                LOG.info("Server is down.");
            }
            
            return false;
        }
        
        if (connection.isAudioStream()) {
            
            synchronized(streams) {
                
                if (streams.size() < config.getMaxConnections()) {
                    streams.add(connection);
                    
                } else {
                    
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Connection limit reached");
                    }
                    
                    return false;
                }
            }
            
        } else {
            
            synchronized(connections) {
                
                if (connections.size() < config.getMaxConnections()) {
                    connections.add(connection);
                    
                } else {
                    
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Connection limit reached");
                    }
                    
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Accepts an incoming connection.
     */
    private boolean accept(Socket socket) throws IOException {

        if (filter != null && !filter.accept(socket.getInetAddress())) {

            if (LOG.isInfoEnabled()) {
                LOG.info("DaapFilter refused connection from " + socket);
            }

            return false;
        }

        DaapConnectionBIO connection = new DaapConnectionBIO(this, socket);

        Thread connThread = threadFactory.createDaapThread(connection,
                "DaapConnectionThread-" + (++threadNo));
        connThread.start();

        return true;
    }
    
    /**
     * Call this to notify the server that Library has changed
     */
    public void update() {
        synchronized(connections) {
            Iterator it = connections.iterator();
            while(it.hasNext()) {
                
                DaapConnectionBIO conn = (DaapConnectionBIO)it.next();
                
                try {
                    conn.update();
                } catch (IOException err) {
                    LOG.error(err);
                }
            }
        }
    }
    
    /**
     * Removes connection from the internal connection pool
     */
    void removeConnection(DaapConnectionBIO connection) {
        
        if (connection.isAudioStream()) {
            
            synchronized(streams) {
                streams.remove(connection);
            }
            
        } else {
            
            synchronized(connections) {
                connections.remove(connection);
            }
            
            DaapSession session = connection.getSession(false);
            if (session != null) {
                session.invalidate();
                
                synchronized(sessionIds) {
                    sessionIds.remove(session.getSessionId());
                }
            }
        }
    }
    
    /**
     * Returns <code>true</code> if sessionId is known and valid
     */
    boolean isSessionIdValid(int sessionId) {
        return isSessionIdValid(new Integer(sessionId));
    }
    
    /**
     * Returns <code>true</code> if sessionId is known and valid
     * 
     * <p>DO NOT CALL THIS METHOD! THIS METHOD IS ONLY PUBLIC 
     * DUE TO SOME DESIGN ISSUES!</p>
     */
    public boolean isSessionIdValid(Integer sessionId) {
        synchronized(sessionIds) {
            return sessionIds.contains(sessionId);
        }
    }
    
    /**
     * Retrieves a DaapConnection for a session ID or <code>null</code>.
     * 
     * <p>DO NOT CALL THIS METHOD! THIS METHOD IS ONLY PUBLIC 
     * DUE TO SOME DESIGN ISSUES!</p>
     * 
     * @param sessionId a session ID
     * @return a DaapConnection or <code>null</code>
     */ 
    public DaapConnection getConnection(Integer sessionId) {
        synchronized(connections) {
            Iterator it = connections.iterator();
            while(it.hasNext()) {
                DaapConnection connection = (DaapConnection)it.next();
                DaapSession session = connection.getSession(false);
                if (session != null) {
                    Integer sid = session.getSessionId();
                    if (sid.equals(sessionId)) {
                        return connection;
                    }
                }
            }
            return null;
        }
    }
    
    /**
     * Creates and unique sessionId and returns it
     * 
     * <p>DO NOT CALL THIS METHOD! THIS METHOD IS ONLY PUBLIC 
     * DUE TO SOME DESIGN ISSUES!</p>
     */
    public Integer createSessionId() {
        synchronized(sessionIds) {
            Integer sid = DaapUtil.createSessionId(sessionIds);
            sessionIds.add(sid);
            return sid;
        }
    }
    
    /**
     * Returns the number of connections
     */
    public int getNumberOfConnections() {
        if (connections == null)
            return 0;
        
        synchronized(connections) {
            return connections.size();
        }
    }
    
    /**
     * Returns the number of streams
     */
    public int getNumberOfStreams() {
        if (streams == null)
            return 0;
        
        synchronized(streams) {
            return streams.size();
        }
    }
    
    /**
     * The run loop
     */
    public void run() {
        
        threadNo = 0;
        running = true;
        
        try {
            
            while(running) {
                Socket socket = ssocket.accept();
                
                try {
                    
                    if (running && ! accept(socket) ) {
                        
                        socket.close();
                    }
                    
                } catch (IOException err) {
                    LOG.error(err);
                    try {
                        socket.close();
                    } catch(IOException ignored) {}
                }
                
                Thread.sleep(100);
            }
            
        } catch (InterruptedException err) {
            LOG.error(err);
         //   throw new RuntimeException(err);
            
        } catch (SocketException err) {
            if (running) {
                LOG.error(err);
            }
          //  throw new RuntimeException(err);
            
        } catch (IOException err) {
            LOG.error(err);
          //  throw new RuntimeException(err);
            
        } finally {
            stop();
        }
    }
    
    /**
     * The default DaapThreadFactory
     */
    private static class DaapThreadFactoryImpl implements DaapThreadFactory {
        
        private DaapThreadFactoryImpl() {    
        }
        
        public Thread createDaapThread(Runnable runnable, String name) {
            Thread thread = new Thread(runnable, name);
            thread.setDaemon(true);
            return thread;
        }
    }
}
