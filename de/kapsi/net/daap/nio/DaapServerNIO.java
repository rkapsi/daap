package de.kapsi.net.daap.nio;

import java.io.IOException;

import java.net.InetAddress;
import java.net.SocketAddress;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;

import de.kapsi.net.daap.DaapUtil;
import de.kapsi.net.daap.Library;
import de.kapsi.net.daap.DaapServer;
import de.kapsi.net.daap.DaapConfig;
import de.kapsi.net.daap.DaapFilter;
import de.kapsi.net.daap.SimpleConfig;
import de.kapsi.net.daap.DaapAuthenticator;
import de.kapsi.net.daap.DaapStreamSource;
import de.kapsi.net.daap.DaapConnection;
import de.kapsi.net.daap.SimpleConfig;
import de.kapsi.net.daap.DaapSession;

import de.kapsi.net.daap.chunks.ContentCodesResponseImpl;
import de.kapsi.net.daap.chunks.ServerInfoResponseImpl;

import de.kapsi.net.daap.chunks.impl.ServerInfoResponse;
import de.kapsi.net.daap.chunks.impl.ContentCodesResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A NIO based DAAP server
 */
public class DaapServerNIO implements DaapServer {
    
    private static final Log LOG = LogFactory.getLog(DaapServerNIO.class);
    
    private ServerSocketChannel ssc = null;
    private Selector selector = null;
    
    private Library library;
    private ServerInfoResponse serverInfo;
    private ContentCodesResponse contentCodes;
    
    private HashSet streams;
    private HashSet connections;
    
    private HashSet sessionIds;
    
    private DaapConfig config;
    
    private DaapFilter filter;
    private DaapStreamSource streamSource;
    private DaapAuthenticator authenticator;
    
    private boolean running = false;
    private boolean update = false;
    
    /**
     *
     * @param library
     */    
    public DaapServerNIO(Library library) {
        this(library, new SimpleConfig());
    }
    
    /**
     *
     * @param library
     * @param port
     */    
    public DaapServerNIO(Library library, int port) {
        this(library, new SimpleConfig(port));
    }
    
    /**
     *
     * @param library
     * @param config
     */    
    public DaapServerNIO(Library library, DaapConfig config) {
        
        this.library = library;
        this.config = config;
        
        serverInfo = new ServerInfoResponseImpl(library.getName());
        contentCodes = new ContentCodesResponseImpl();
    }
    
    /**
     *
     * @return
     */    
    public Library getLibrary() {
        return library;
    }
    
    /**
     *
     * @return
     */    
    public ServerInfoResponse getServerInfoResponse() {
        return serverInfo;
    }
    
    /**
     *
     * @return
     */    
    public ContentCodesResponse getContentCodesResponse() {
        return contentCodes;
    }
    
    /**
     *
     * @param config
     */    
    public void setConfig(DaapConfig config) {
        this.config = config;
    }
    
    /**
     *
     * @param authenticator
     */    
    public void setAuthenticator(DaapAuthenticator authenticator) {
        this.authenticator = authenticator;
    }
    
    /**
     *
     * @return
     */    
    public DaapAuthenticator getAuthenticator() {
        return authenticator;
    }
    
    /**
     *
     * @param streamSource
     */    
    public void setStreamSource(DaapStreamSource streamSource) {
        this.streamSource = streamSource;
    }
    
    /**
     *
     * @return
     */    
    public DaapStreamSource getStreamSource() {
        return streamSource;
    }
    
    /**
     *
     * @param filter
     */    
    public void setFilter(DaapFilter filter) {
        this.filter = filter;
    }
    
    /**
     *
     * @return
     */    
    public DaapFilter getFilter() {
        return filter;
    }
    
    /**
     *
     * @return
     */    
    public DaapConfig getConfig() {
        return config;
    }
    
    /**
     * Returns <tt>true</tt> if DAAP Server
     * accepts incoming connections.
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     *
     * @throws IOException
     */    
    public void bind() throws IOException {
        
        SocketAddress bindAddr = config.getSocketAddress();
        int backlog = config.getBacklog();
        
        streams = new HashSet();
        connections = new HashSet();
        
        sessionIds = new HashSet();
        
        try {

            ssc = ServerSocketChannel.open();
            ssc.socket().bind(bindAddr, backlog);
            ssc.configureBlocking(false);
            
            selector = Selector.open();
            
            if (LOG.isInfoEnabled()) {
                LOG.info("DaapServerNIO bound to " + bindAddr);
            }
            
        } catch (IOException err) {
            close();
            throw err;
        }
    }
    
    /**
     * Call this to notify the server that Library has changed
     */
    public void update() {
        if (isRunning()) {
            update = true;
            selector.wakeup();
        }
    }
    
    /**
     * Returns <tt>true</tt> if sessionId is known and valid
     */
    public boolean isSessionIdValid(int sessionId) {
        return isSessionIdValid(new Integer(sessionId));
    }
    
    /**
     *
     * @param sessionId
     * @return
     */    
    public boolean isSessionIdValid(Integer sessionId) {
        return sessionIds.contains(sessionId);
    }
    
    /**
     *
     * @return
     */    
    public Integer createSessionId() {
        Integer sid = DaapUtil.createSessionId(sessionIds);
        sessionIds.add(sid);
        return sid;
    }
    
    /**
     * Returns the number of connections
     */
    public int getNumberOfConnections() {
        return (connections != null) ? connections.size() : 0;
    }
    
    /**
     * Returns the number of streams
     */
    public int getNumberOfStreams() {
        return (streams != null) ? streams.size() : 0;
    }
    
   /**
    * Stops the DAAP Server
    */
    public synchronized void stop() {
        if (selector != null) {
            if (running) {
                running = false;
                selector.wakeup();
            } else {
                close();
            }
        }
    }
    
    /**
     *
     */
    private void close() {
        
        running = false;
        update = false;
       
        try {
            
            if (selector != null) {
                
                // BEGIN WORKAROUND
                // Selector.close() doesn't close properly on OSX
                // (Java 1.4.2_04) and leaves something in an 
                // undefined state with the result that our Server
                // PORT can not be longer used (no exception etc.
                // the Selector just hangs at Selector.select()).
                // This workaround is a combination of Bug: 5004075
                // and http://forum.java.sun.com/thread.jsp?forum=31&thread=384019
                
                Iterator it = selector.keys().iterator();
                while(it.hasNext()) {
                    SelectionKey sk = (SelectionKey)it.next();
                    cancel(sk);
                }
                
                selector.selectNow();
                // END WORKAROUND
                
                // Note: throws on OSX always "IOEx: Bad file descriptor"
                selector.close(); 
            }
            
        } catch (IOException err) {
            LOG.error("Selector.close()", err);
        }

        selector = null;
        ssc = null;
       
        if (sessionIds != null)
            sessionIds.clear();
        
        sessionIds = null;
        
        streams = null;
        connections = null;
    }
    
    /**
     * Disconnects all DAAP and Stream connections
     */
    public void disconnectAll() {
        if (selector != null) {
            
            Set keys = selector.keys();
            
            synchronized(keys) {
                Iterator it = keys.iterator();
                while(it.hasNext()) {
                    SelectionKey sk = (SelectionKey)it.next();
                    SelectableChannel channel = (SelectableChannel)sk.channel();
                    if (channel instanceof SocketChannel) {
                        cancel(sk);
                    }
                }
            }
        }
    }
   
    /**
     *
     */
    private void cancel(SelectionKey sk) {
        
        SelectableChannel channel = (SelectableChannel)sk.channel();
        
        try {
            channel.close();
        } catch (IOException err) {
            LOG.error("Channel.close()", err);
        }
        
        sk.cancel();
        
        DaapConnection connection = (DaapConnection)sk.attachment();
        
        if (connection != null) {
            
            DaapSession session = connection.getSession(false);
            if (session != null) {
                sessionIds.remove(session.getSessionId());
            }
            
            connection.close();
            
            if (connection.isNormal()) {
                connections.remove(connection);
            } else if (connection.isAudioStream()) {
                streams.remove(connection);
            }
        }
    }
    
    /**
     *
     * @throws IOException
     */
    void registerConnection(DaapConnection connection) throws IOException {
        
        if (connection.isAudioStream()) {
            
            if (streams.size() < config.getMaxConnections()) {
                streams.add(connection);
                return;
            }
            
        } else if (connection.isNormal()) {
            
            if (connections.size() < config.getMaxConnections()) {
                connections.add(connection);
                return;
            }
        }
        
        throw new IOException("Too many connections");
    }
    
    /**
     * 
     * @throws IOException
     */
    private boolean accept(InetAddress addr) {
        
        if (filter != null && filter.accept(addr) == false) {
            
            if (LOG.isInfoEnabled()) {
                LOG.info("DaapFilter refused connection from " + addr);
            }
            
            return false;
        }
        
        return true;
    }
    
    /**
     *
     * @throws IOException
     */
    private void processAccept(SelectionKey sk) throws IOException {
        
        if (!sk.isValid())
            return;
        
        ServerSocketChannel ssc = (ServerSocketChannel)sk.channel();
        SocketChannel channel = ssc.accept();

        if (channel.isOpen() && accept(channel.socket().getInetAddress())) {

            channel.configureBlocking(false);

            DaapConnection connection 
                = new DaapConnectionImpl(this, channel);
            
            SelectionKey key = channel.register(selector, SelectionKey.OP_READ, connection);
            
        } else {
            try {
                channel.close();
            } catch (IOException err) {
                LOG.error("SocketChannel.close()", err);
            }
        }
    }
    
    /**
     *
     * @throws IOException
     */
    private void processRead(SelectionKey sk) throws IOException {
        
        if (!sk.isValid())
            return;
        
        DaapConnectionImpl connection = (DaapConnectionImpl)sk.attachment(); 
        SocketChannel channel = (SocketChannel)sk.channel();
        
        boolean keepAlive = false;
        
        keepAlive = connection.read();
        
        if (keepAlive) {
            sk.interestOps(connection.interrestOps());
        } else {
            cancel(sk);
        }
    }
    
    /**
     *
     * @throws IOException
     */
    private void processWrite(SelectionKey sk) throws IOException {
        
        if (!sk.isValid())
            return;
        
        DaapConnectionImpl connection = (DaapConnectionImpl)sk.attachment();
        SocketChannel channel = (SocketChannel)sk.channel();
        
        boolean keepAlive = false;

        try {
            keepAlive = connection.write();
        } catch (DaapStreamException err) {
            
            // Broken pipe: User pressed Pause, fast-foward 
            // or whatever. Just close the connection and go 
            // ahead
            keepAlive = false;
            //LOG.error(err);
        }
        
        if (keepAlive) {
            sk.interestOps(connection.interrestOps());

        } else {
            cancel(sk);
        }
    }
    
    /**
     *
     */
    private void processUpdate() {
        
        Set keys = selector.keys();
            
        synchronized(keys) {
            Iterator it = keys.iterator();
            while(it.hasNext()) {
                SelectionKey sk = (SelectionKey)it.next();
                SelectableChannel channel = (SelectableChannel)sk.channel();
                
                if (channel instanceof SocketChannel) {
                    
                    DaapConnection connection = (DaapConnection)sk.attachment();
                    
                    if (connection.isNormal()) {
                        try {
                            connection.update();
                            sk.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        } catch (ClosedChannelException err) {
                            cancel(sk);
                            LOG.error("DaapConnection.update()", err);
                        }  catch (IOException err) {
                            cancel(sk);
                            LOG.error("DaapConnection.update()", err);
                        }
                    }
                }
            }
        }
    }
    
    /**
     *
     * @throws IOException
     */
    private void process() throws IOException {
       
        int n = -1;
        
        while(running) {
            
            try {
                n = selector.select();
            } catch (NullPointerException err) {
                continue;
            } catch (CancelledKeyException err) {
                continue;
            }
            
            if (!running) {
                break;
            }
            
            if (update)
                processUpdate();
            update = false;
            
            if (n == 0)
                continue;
            
            Iterator it = selector.selectedKeys().iterator();
            
            while (it.hasNext() && running) {
                SelectionKey sk = (SelectionKey)it.next();
                it.remove();
                
                try {
                    if (sk.isAcceptable()) {
                        processAccept(sk);

                    } else {
                       
                        try {

                            if (sk.isReadable()) {
                                processRead(sk);
                                
                            } else if (sk.isWritable()) {
                                processWrite(sk);
                            }
 
                        } catch (IOException err) {
                            cancel(sk);
                            LOG.error("Exception in processRead() or processWrite()", err);
                        }
                    }
                } catch (CancelledKeyException err) {
                    continue;
                }
            }
        }
    }
    
    public void run() {

        try {
            
            if (running) {
                LOG.error("DaapServerNIO is already running.");
                return;
            }
            
            SelectionKey sk = ssc.register(selector, SelectionKey.OP_ACCEPT);
            
            running = true;
            process();
            
        } catch (IOException err) {
            LOG.error(err);
            
        } finally {
            close();
        }
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Name: ").append(config.getServerName()).append("\n");
        buffer.append("Address: ").append(config.getSocketAddress()).append("\n");
        buffer.append("Backlog: ").append(config.getBacklog()).append("\n");
        buffer.append("Max connections: ").append(config.getMaxConnections()).append("\n");
        buffer.append("IsRunning: ").append(isRunning()).append("\n");
        
        if (isRunning()) {
            buffer.append("Connections: ").append(getNumberOfConnections()).append("\n");
            buffer.append("Streams: ").append(getNumberOfStreams()).append("\n");
        }
        
        return buffer.toString();
    }
}

