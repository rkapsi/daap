
package de.kapsi.net.daap.bio;

import java.io.IOException;

import java.net.SocketAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketException;

import java.util.Iterator;
import java.util.HashSet;

import de.kapsi.net.daap.DaapUtil;
import de.kapsi.net.daap.Library;
import de.kapsi.net.daap.DaapServer;
import de.kapsi.net.daap.DaapConfig;
import de.kapsi.net.daap.DaapFilter;
import de.kapsi.net.daap.SimpleConfig;
import de.kapsi.net.daap.DaapAuthenticator;
import de.kapsi.net.daap.DaapStreamSource;
import de.kapsi.net.daap.SimpleConfig;
import de.kapsi.net.daap.DaapRequest;
import de.kapsi.net.daap.DaapSession;
import de.kapsi.net.daap.DaapConnection;

import de.kapsi.net.daap.chunks.ContentCodesResponseImpl;
import de.kapsi.net.daap.chunks.ServerInfoResponseImpl;

import de.kapsi.net.daap.chunks.impl.ServerInfoResponse;
import de.kapsi.net.daap.chunks.impl.ContentCodesResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This DAAP server is written with the standard 1 Thread per
 * connection pattern.
 */
public class DaapServerBIO implements DaapServer {
    
    private static final Log LOG = LogFactory.getLog(DaapServerBIO.class);
    
    private int threadNo = 0;
    
    private Library library;
    private ServerInfoResponse serverInfo;
    private ContentCodesResponse contentCodes;
    
    private DaapFilter filter;
    
    private HashSet sessionIds;
    private HashSet connections;
    private HashSet streams;
    
    private DaapConfig config;
    private DaapAuthenticator authenticator;
    private DaapStreamSource streamSource;
    
    private DaapServer server;
    private ServerSocket ssocket;
    
    private boolean running = false;
    
    public DaapServerBIO(Library library) {
        this(library, new SimpleConfig());
    }
    
    public DaapServerBIO(Library library, int port) {
        this(library, new SimpleConfig(port));
    }
    
    public DaapServerBIO(Library library, DaapConfig config) {
        
        this.library = library;
        this.config = config;
        
        serverInfo = new ServerInfoResponseImpl(library.getName());
        contentCodes = new ContentCodesResponseImpl();
        
        sessionIds = new HashSet();
        connections = new HashSet();
        streams = new HashSet();
    }
    
    public Library getLibrary() {
        return library;
    }
    
    public ServerInfoResponse getServerInfoResponse() {
        return serverInfo;
    }
    
    public ContentCodesResponse getContentCodesResponse() {
        return contentCodes;
    }
    
    public void setConfig(DaapConfig config) {
        this.config = config;
    }
    
    public void setAuthenticator(DaapAuthenticator authenticator) {
        this.authenticator = authenticator;
    }
    
    public DaapAuthenticator getAuthenticator() {
        return authenticator;
    }
    
    public void setStreamSource(DaapStreamSource streamSource) {
        this.streamSource = streamSource;
    }
    
    public DaapStreamSource getStreamSource() {
        return streamSource;
    }
    
    public void setFilter(DaapFilter filter) {
        this.filter = filter;
    }
    
    public DaapFilter getFilter() {
        return filter;
    }
    
    public DaapConfig getConfig() {
        return config;
    }
    
    public synchronized void bind() throws IOException {
        if (running)
            return;
        
        SocketAddress bindAddr = config.getSocketAddress();
        int backlog = config.getBacklog();
        
        
        ssocket = new ServerSocket();
        ssocket.bind(bindAddr, backlog);
        
        if (LOG.isInfoEnabled()) {
            LOG.info("DaapServerBIO bound to " + bindAddr);
        }
    }
    
    /**
     * Returns <tt>true</tt> if DAAP Server
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
        } catch (IOException err) {}
        
        disconnectAll();
    }
    
    /**
     * Disconnects all DAAP and Stream connections
     */
    public synchronized void disconnectAll() {
        
        if (!running)
            return;
        
        synchronized(connections) {
            Iterator it = connections.iterator();
            while(it.hasNext()) {
                ((DaapConnectionBIO)it.next()).close();
            }
            connections.clear();
        }
        
        synchronized(streams) {
            Iterator it = streams.iterator();
            while(it.hasNext()) {
                ((DaapConnectionBIO)it.next()).close();
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
    public synchronized boolean addConnection(DaapConnectionBIO connection) {
        
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
     * Called by DaapAcceptor
     */
    public boolean accept(Socket socket)
        throws IOException {
        
        
        if (filter != null &&
        filter.accept(socket.getInetAddress()) == false) {
            
            if (LOG.isInfoEnabled()) {
                LOG.info("DaapFilter refused connection from " + socket);
            }
            
            return false;
        }
        
        DaapConnectionBIO connection = new DaapConnectionBIO(this, socket);
        
        Thread connThread = new Thread(connection, "DaapConnectionThread-" + (++threadNo));
        connThread.setDaemon(true);
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
    public void removeConnection(DaapConnectionBIO connection) {
        
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
     * Returns <tt>true</tt> if sessionId is known and valid
     */
    public boolean isSessionIdValid(int sessionId) {
        return isSessionIdValid(new Integer(sessionId));
    }
    
    /**
     * Returns <tt>true</tt> if sessionId is known and valid
     */
    public boolean isSessionIdValid(Integer sessionId) {
        synchronized(sessionIds) {
            return sessionIds.contains(sessionId);
        }
    }
    
    /**
     * Returns an unique session-id
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
                    
                } catch (IOException sErr) {
                    LOG.error(sErr);
                    socket.close();
                }
                
                Thread.sleep(100);
            }
            
        } catch (InterruptedException err) {
            LOG.error(err);
        } catch (SocketException err) {
            LOG.error(err);
        } catch (IOException err) {
            LOG.error(err);
        } finally {
            stop();
        }
    }
    
}
