package de.kapsi.net.daap.nio;

import java.io.*;
import java.net.*;
import java.util.*;

import java.nio.*;
import java.nio.channels.*;
import java.nio.channels.spi.*;

import de.kapsi.net.daap.DaapUtil;
import de.kapsi.net.daap.Library;
import de.kapsi.net.daap.DaapServer;
import de.kapsi.net.daap.DaapConfig;
import de.kapsi.net.daap.DaapFilter;
import de.kapsi.net.daap.SimpleConfig;
import de.kapsi.net.daap.DaapAuthenticator;
import de.kapsi.net.daap.DaapStreamSource;
import de.kapsi.net.daap.SimpleConfig;

import de.kapsi.net.daap.chunks.ContentCodesResponseImpl;
import de.kapsi.net.daap.chunks.ServerInfoResponseImpl;

import de.kapsi.net.daap.chunks.impl.ServerInfoResponse;
import de.kapsi.net.daap.chunks.impl.ContentCodesResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The famous DaapServer.
 */
public class DaapServerNIO implements DaapServer {
    
    private static final Log LOG = LogFactory.getLog(DaapServerNIO.class);
    
    private ServerSocketChannel ssc = null;
    private Selector selector = null;
    
    private Library library;
    private ServerInfoResponse serverInfo;
    private ContentCodesResponse contentCodes;
    
    private HashSet sessionIds;
    private HashSet connections;
    
    private DaapConfig config;
    
    private DaapFilter filter;
    private DaapStreamSource streamSource;
    private DaapAuthenticator authenticator;
    
    private boolean running = false;
    private boolean update = false;
    
    public DaapServerNIO(Library library) {
        this(library, new SimpleConfig());
    }
    
    public DaapServerNIO(Library library, int port) {
        this(library, new SimpleConfig(port));
    }
    
    public DaapServerNIO(Library library, DaapConfig config) {
        
        this.library = library;
        this.config = config;
        
        serverInfo = new ServerInfoResponseImpl(library.getName());
        contentCodes = new ContentCodesResponseImpl();
    }
    
    public void bind() throws IOException {
        
        int port = config.getPort();
        InetAddress bindAddr = config.getBindAddress();
        int backlog = config.getBacklog();
        
        connections = new HashSet();
        sessionIds = new HashSet();
        
        try {
            
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);

            if (bindAddr == null)
                bindAddr = InetAddress.getLocalHost();

            InetSocketAddress addr = new InetSocketAddress(bindAddr, port);

            ssc.socket().bind(addr, backlog);
            selector = Selector.open();

            if (LOG.isInfoEnabled()) {
                LOG.info("DaapServerNIO bound to " + addr);
            }
            
        } catch (IOException err) {
            close();
            throw err;
        }
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
    
    /**
     * Returns <tt>true</tt> if DAAP Server
     * accepts incoming connections.
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Stops the DAAP Server
     */
    public void stop() {
        
        if (isRunning()){
            disconnectAll();
            close();
        }
    }
    
    /**
     * Disconnects all DAAP and Stream connections
     */
    public void disconnectAll() {
        if (isRunning()) {
            
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
    
    public boolean isSessionIdValid(Integer sessionId) {
        return sessionIds.contains(sessionId);
    }
    
    public Integer createSessionId() {
        Integer sid = DaapUtil.createSessionId(sessionIds);
        sessionIds.add(sid);
        return sid;
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
        /*if (streams == null)
            return 0;
        
        synchronized(streams) {
            return streams.size();
        }*/
        return 0;
    }
    
    private void close() {
        
        running = false;
        update = false;
        
        try {
            if (ssc != null)
                ssc.close();
        } catch (IOException err) {
            LOG.error(err);
        }
        
        try {
            if (selector != null)
                selector.close(); 
        } catch (IOException err) {
            LOG.error(err);
        }
        
        selector = null;
        ssc = null;
       
        if (sessionIds != null)
            sessionIds.clear();
        
        sessionIds = null;
    }
    
    private void processAccept(SelectionKey sk) throws IOException {
        
        if (!sk.isValid())
            return;
        
        ServerSocketChannel ssc = (ServerSocketChannel)sk.channel();
        SocketChannel channel = ssc.accept();

        if (channel.isOpen() && accept(channel.socket().getInetAddress())) {

            channel.configureBlocking(false);

            DaapConnection connection 
                = new DaapConnection(this, channel);
            
            SelectionKey key 
                = channel.register(selector, SelectionKey.OP_READ, connection);
            
        } else {

            cancel(sk);
        }
    }
    
    private void cancel(SelectionKey sk) {
        
        sk.cancel();
        
        try {
            sk.channel().close();
        } catch (IOException err) {
            LOG.error(err);
        }
    }
    
    private void processRead(SelectionKey sk) throws IOException {
        
        if (!sk.isValid())
            return;
        
        DaapConnection connection = (DaapConnection)sk.attachment(); 
        SocketChannel channel = (SocketChannel)sk.channel();
        
        boolean keepAlive = false;
        
        keepAlive = connection.read();
        
        if (keepAlive) {
            sk.interestOps(connection.interrestOps());
        } else {
            cancel(sk);
        }
    }
    
    private void processWrite(SelectionKey sk) throws IOException {
        
        if (!sk.isValid())
            return;
        
        DaapConnection connection = (DaapConnection)sk.attachment();
        SocketChannel channel = (SocketChannel)sk.channel();
        
        boolean keepAlive = false;

        keepAlive = connection.write();

        if (keepAlive) {
            sk.interestOps(connection.interrestOps());

        } else {
            cancel(sk);
        }
    }
    
    private void processUpdate() {
        
        Set keys = selector.keys();
            
        synchronized(keys) {
            Iterator it = keys.iterator();
            while(it.hasNext()) {
                SelectionKey sk = (SelectionKey)it.next();
                SelectableChannel channel = (SelectableChannel)sk.channel();
                
                if (channel instanceof SocketChannel) {
                    
                    DaapConnection connection = (DaapConnection)sk.attachment();
                    
                    try {
                        connection.update();
                        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, connection);
                    } catch (ClosedChannelException err) {
                        LOG.error("Error while updating", err);
                        cancel(sk);
                    }  catch (IOException err) {
                        LOG.error("Error while updating", err);
                        cancel(sk);
                    }
                }
            }
        }
    }
    
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
                            LOG.error(err);
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
            
            if (ssc == null || selector == null) {
                throw new IOException("DaapServerNIO is not bound.");
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
}

