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

package de.kapsi.net.daap.nio;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.DaapAuthenticator;
import de.kapsi.net.daap.DaapConfig;
import de.kapsi.net.daap.DaapConnection;
import de.kapsi.net.daap.DaapFilter;
import de.kapsi.net.daap.DaapServer;
import de.kapsi.net.daap.DaapSession;
import de.kapsi.net.daap.DaapStreamException;
import de.kapsi.net.daap.DaapStreamSource;
import de.kapsi.net.daap.DaapThreadFactory;
import de.kapsi.net.daap.DaapUtil;
import de.kapsi.net.daap.Library;
import de.kapsi.net.daap.SimpleConfig;

/**
 * A DAAP server written with NIO and a singe Thread.
 *
 * @author  Roger Kapsi
 */
public class DaapServerNIO implements DaapServer {
    
    private static final Log LOG = LogFactory.getLog(DaapServerNIO.class);
    
    private static final long TIMEOUT = 50;
    
    private ServerSocketChannel ssc = null;
    private Selector selector = null;
    
    private Library library;
    
    private HashSet streams;
    private HashSet connections;
    
    private HashSet sessionIds;
    
    private DaapConfig config;
    
    private DaapFilter filter;
    private DaapStreamSource streamSource;
    private DaapAuthenticator authenticator;
    
    private boolean running = false;
    private boolean disconnectAll = false;
    private boolean update = false;
    
    /**
     * Creates a new DAAP server with Library and {@see SimpleConfig}
     * 
     * @param library a Library
     */    
    public DaapServerNIO(Library library) {
        this(library, new SimpleConfig());
    }
    
    /**
     * Creates new DAAP server with Library, a {@see SimpleConfig} and 
     * the Port
     * 
     * @param library a Library
     * @param port a Port used by SimpleConfig
     */    
    public DaapServerNIO(Library library, int port) {
        this(library, new SimpleConfig(port));
    }
    
    /**
     * Creates a new DAAP server with Library and DaapConfig
     * 
     * @param library a Library
     * @param config a DaapConfig
     */    
    public DaapServerNIO(Library library, DaapConfig config) {
        this.library = library;
        this.config = config;
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
     * Throws an {@see java.lang.UnsupportedOperationException} as
     * the NIO server is implemented with a singe Thread.
     */
    public void setThreadFactory(DaapThreadFactory factory) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Returns <code>true</code> if DAAP Server
     * accepts incoming connections.
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Binds this server to the SocketAddress supplied by DaapConfig
     * 
     * @throws IOException
     */    
    public void bind() throws IOException {
        
        SocketAddress bindAddr = config.getInetSocketAddress();
        int backlog = config.getBacklog();
        
        streams = new HashSet();
        connections = new HashSet();
        
        sessionIds = new HashSet();
        
        try {

            ssc = ServerSocketChannel.open();
            ServerSocket socket = ssc.socket();
            
            // BugID: 4546610
            // On Win2k, Mac OS X, XYZ it is possible to bind
            // the same address without rising a SocketException
            // (the Documentation lies)
            socket.setReuseAddress(false);
            
            try {
                socket.bind(bindAddr, backlog);
            } catch (SocketException err) {
                throw new BindException(err.getMessage());
            }
            
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
        update = true;
    }
    
    /**
     * Returns <code>true</code> if sessionId is known and valid
     */
    public synchronized boolean isSessionIdValid(int sessionId) {
        return isSessionIdValid(new Integer(sessionId));
    }
    
    /**
     *
     * @param sessionId
     * @return
     */    
    public synchronized boolean isSessionIdValid(Integer sessionId) {
        return sessionIds.contains(sessionId);
    }
    
    /**
     *
     * @param sessionId
     * @return
     */ 
    public DaapConnection getConnection(Integer sessionId) {
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
    public synchronized int getNumberOfConnections() {
        return (connections != null) ? connections.size() : 0;
    }
    
    /**
     * Returns the number of streams
     */
    public synchronized int getNumberOfStreams() {
        return (streams != null) ? streams.size() : 0;
    }
    
   /**
    * Stops the DAAP Server
    */
    public void stop() {
        running = false;
    }
    
    /**
     *
     */
    private synchronized void close() {
        
        running = false;
        update = false;
        disconnectAll = false;
        
        if (selector != null) {
            
            Iterator it = selector.keys().iterator();
            while(it.hasNext()) {
                SelectionKey sk = (SelectionKey)it.next();
                cancel(sk);
            }
                
            try {     
                // Note: throws on OSX always "IOEx: Bad file descriptor"
                selector.close();
            } catch (IOException err) {
                LOG.error("Selector.close()", err);
            }
            
            selector = null;
        }
        
        if (ssc != null) {
            try {
                ssc.close();
            } catch (IOException err) {
                LOG.error("ServerSocketChannel.close()", err);
            }
            ssc = null;
        }
       
        if (sessionIds != null) {
            sessionIds.clear();
            sessionIds = null;
        }
        
        if (streams != null) {
            streams.clear();
            streams = null;
        }
        
        if (connections != null) {
            connections.clear();
            connections = null;
        }
    }
    
    /**
     * Disconnects all DAAP and Stream connections
     */
    public void disconnectAll() {
        disconnectAll = true;
    }
   
    /**
     * Cancel SelesctionKey, close Channel and "free" the attachment
     */
    private void cancel(SelectionKey sk) {
        
        sk.cancel();
        
        SelectableChannel channel = (SelectableChannel)sk.channel();
        
        sk.cancel();
        
        try {
            channel.close();
        } catch (IOException err) {
            LOG.error("Channel.close()", err);
        }

        DaapConnection connection = (DaapConnection)sk.attachment();
        
        if (connection != null) {
            
            DaapSession session = connection.getSession(false);
            if (session != null) {
                sessionIds.remove(session.getSessionId());
            }
            
            connection.close();
            
            if (connection.isDaapConnection()) {
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
            
        } else if (connection.isDaapConnection()) {
            
            if (connections.size() < config.getMaxConnections()) {
                connections.add(connection);
                return;
            }
        }
        
        throw new IOException("Too many connections");
    }
    
    /**
     * Returns <code>true</code> if host with <code>addr</code> is
     * allowed to connect to this DAAP server.
     * 
     * @return true host with <code>addr</code> is allowed to connect
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
     * Accept an icoming connection
     * 
     * @throws IOException
     */
    private void processAccept(SelectionKey sk) throws IOException {
        
        if (!sk.isValid())
            return;
        
        ServerSocketChannel ssc = (ServerSocketChannel)sk.channel();
        SocketChannel channel = ssc.accept();
        
        if (channel == null)
            return;
        
        if (channel.isOpen() && accept(channel.socket().getInetAddress())) {

            channel.configureBlocking(false);

            DaapConnection connection 
                = new DaapConnectionNIO(this, channel);
            
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
     * Read data
     * 
     * @throws IOException
     */
    private void processRead(SelectionKey sk) throws IOException {
        
        if (!sk.isValid())
            return;
        
        DaapConnectionNIO connection = (DaapConnectionNIO)sk.attachment(); 
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
     * Write data
     * 
     * @throws IOException
     */
    private void processWrite(SelectionKey sk) throws IOException {
        
        if (!sk.isValid())
            return;
        
        DaapConnectionNIO connection = (DaapConnectionNIO)sk.attachment();
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
     * Disconnects all clients from this server
     */
    private void processDisconnect() {
        Iterator it = selector.keys().iterator();
        while(it.hasNext()) {
            SelectionKey sk = (SelectionKey)it.next();
            SelectableChannel channel = (SelectableChannel)sk.channel();
            if (channel instanceof SocketChannel) {
                cancel(sk);
            }
        }
    }
    
    /**
     * Notify all clients about an update of the Library
     */
    private void processUpdate() {
        
        Set keys = selector.keys(); 
        Iterator it = keys.iterator();
        while(it.hasNext()) {
            SelectionKey sk = (SelectionKey)it.next();
            SelectableChannel channel = (SelectableChannel)sk.channel();
            
            if (channel instanceof SocketChannel) {
                
                DaapConnection connection = (DaapConnection)sk.attachment();
                
                if (connection.isDaapConnection()) {
                    try {
                        connection.update();
                        if (sk.isValid()) {
                            try {
                                sk.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                            } catch (CancelledKeyException err) {
                                cancel(sk);
                                LOG.error("SelectionKey.interestOps()", err);
                            }
                        }
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
    
    /**
     * The actual NIO run loop
     * 
     * @throws IOException
     */
    private void process() throws IOException {
       
        int n = -1;
        
        running = true;
        update = false;
        disconnectAll = false;
        
        while(running) {
            
            try {
                n = selector.select(TIMEOUT);
            } catch (NullPointerException err) {
                continue;
            } catch (CancelledKeyException err) {
                continue;
            }
            
            if (!running) {
                break;
            }
            
            if (disconnectAll) {
                processDisconnect();
                disconnectAll = false;
                continue;   // as all clients were disconnected
                            // there is nothing more to do
            }
            
            if (update) {
                processUpdate();
                update = false;
            }
            
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

                        if (sk.isReadable()) {
                            try {
                                processRead(sk);
                            } catch (IOException err) {
                                cancel(sk);
                                LOG.error("An exception occured in processRead()", err);
                            }
                        } else if (sk.isWritable()) {
                            try {
                                processWrite(sk);
                            } catch (IOException err) {
                                cancel(sk);
                                LOG.error("An exception occured in processWrite()", err);
                            }
                        }
                    }
                } catch (CancelledKeyException err) {
                    continue;
                }
            }
        }
        
        // close() is in finally of run() {}
    }
    
    /**
     * 
     */
    public void run() {

        try {
            
            if (running) {
                LOG.error("DaapServerNIO is already running.");
                return;
            }
            
            SelectionKey sk = ssc.register(selector, SelectionKey.OP_ACCEPT);
            
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
        buffer.append("Address: ").append(config.getInetSocketAddress()).append("\n");
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

