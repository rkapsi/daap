
package de.kapsi.net.daap;

import java.io.*;
import java.net.*;
import java.util.*;

import de.kapsi.net.daap.chunks.ServerInfoResponse;
import de.kapsi.net.daap.chunks.ContentCodesResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The famous DaapServer.
 */
public class DaapServer implements Runnable {
    
    private static final Log LOG = LogFactory.getLog(DaapServer.class);
    
    private final static Random generator = new Random();
    
    static {
        // warm up...
        for(int i = 0; i < 100; i++) {
            generator.nextInt();
        }
    }
    
    private int threadNo = 0;
    
    private Library library;
    private ServerInfoResponse serverInfo;
    private ContentCodesResponse contentCodes;
    
    private DaapFilter filter;
    
    private HashSet sessionIds;
    private HashSet connections;
    private HashSet streams;
    
    private DaapConfig config;
    
    private DaapRequestHandler requestHandler;
    
    private DaapServer server;
    private ServerSocket ssocket;
    
    private boolean running = false;
    
    public DaapServer(Library library) {
        this(library, new SimpleConfig());
    }
    
    public DaapServer(Library library, int port) {
        this(library, new SimpleConfig(port));
    }
    
    public DaapServer(Library library, DaapConfig config) {
        
        this.library = library;
        this.config = config;
        
        serverInfo = new ServerInfoResponseImpl(library.getName());
        contentCodes = new ContentCodesResponseImpl();
        
        requestHandler = new DaapRequestHandler(serverInfo, contentCodes, library);
        
        sessionIds = new HashSet();
        connections = new HashSet();
        streams = new HashSet();
    }
    
    public void setConfig(DaapConfig config) {
        this.config = config;
    }
    
    public void setAuthenticator(DaapAuthenticator authenticator) {
        requestHandler.setAuthenticator(authenticator);
    }
    
    public DaapAuthenticator getAuthenticator() {
        return requestHandler.getAuthenticator();
    }
    
    public void setStreamSource(DaapStreamSource streamSource) {
        requestHandler.setStreamSource(streamSource);
    }
    
    public DaapStreamSource getStreamSource() {
        return requestHandler.getStreamSource();
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
    
    public synchronized void init() throws IOException {
        if (running)
            return;
        
        int port = config.getPort();
        int backlog = config.getBacklog();
        InetAddress bindAddr = config.getBindAddress();
        
        ssocket = new ServerSocket(port, backlog, bindAddr);
        
        if (LOG.isInfoEnabled()) {
            if (bindAddr == null) {
                LOG.info("DaapServer bound to port: " + port);
            } else {
                LOG.info("DaapServer bound to " + bindAddr + ":" + port);
            }
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
                ((DaapConnection)it.next()).close();
            }
            connections.clear();
        }
        
        synchronized(streams) {
            Iterator it = streams.iterator();
            while(it.hasNext()) {
                ((DaapConnection)it.next()).close();
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
    public synchronized boolean addConnection(DaapConnection connection) {
        
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
                    connection.connectionKeepAlive();
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
        
        DaapConnection connection = new DaapConnection(this, socket);
        
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
                
                DaapConnection conn = (DaapConnection)it.next();
                
                try {
                    conn.update();
                } catch (IOException err) {
                    LOG.error(err);
                }
            }
        }
    }
    
    /**
     *
     */
    public void processRequest(DaapConnection connection, DaapRequest request)
            throws IOException {
        
        boolean complete = false;
        
        complete = requestHandler.processRequest(connection, request);
        
        if (!complete) {
            connection.connectionClose();
        }
    }
    
    /**
     * Removes connection from the internal connection pool
     */
    public void removeConnection(DaapConnection connection) {
        
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
    Integer createSessionId(DaapConnection conn) {
        
        Integer sessionId = null;
        
        synchronized(sessionIds) {
            
            while(sessionId == null || sessionIds.contains(sessionId)) {
                int tmp = generator.nextInt();
                
                if (tmp == 0) {
                    continue;
                } else if (tmp < 0) {
                    tmp = -tmp;
                }
                
                sessionId = new Integer(tmp);
            }
            
            sessionIds.add(sessionId);
        }
        
        return sessionId;
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
