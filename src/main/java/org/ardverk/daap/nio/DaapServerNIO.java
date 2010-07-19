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

package org.ardverk.daap.nio;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.ardverk.daap.DaapConfig;
import org.ardverk.daap.DaapConnection;
import org.ardverk.daap.DaapServer;
import org.ardverk.daap.DaapSession;
import org.ardverk.daap.DaapStreamException;
import org.ardverk.daap.Library;
import org.ardverk.daap.SessionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DAAP server written with NIO and a single Thread.
 * 
 * @author Roger Kapsi
 */
public class DaapServerNIO extends DaapServer<DaapConnectionNIO> {

    private static final Logger LOG = LoggerFactory
            .getLogger(DaapServerNIO.class);

    /** Selector.select() timeout */
    private static final long TIMEOUT = 250;

    /** The ServerSocket */
    private ServerSocketChannel ssc = null;

    /** Selector for ServerSocket and Sockets */
    private Selector selector = null;

    /** Flag to indicate that all clients shall be disconnected */
    private boolean disconnectAll = false;

    /**
     * Flag to indicate there are Library updates available in the queue
     */
    private boolean update = false;

    /**
     * Creates a new DAAP server with Library and {@see SimpleConfig}
     * 
     * @param library
     *            a Library
     */
    public DaapServerNIO(Library library) {
        this(library, new DaapConfig());
    }

    /**
     * Creates a new DAAP server with Library and DaapConfig
     * 
     * @param library
     *            a Library
     * @param config
     *            a DaapConfig
     */
    public DaapServerNIO(Library library, DaapConfig config) {
        super(library, config);
    }

    /**
     * Binds this server to the SocketAddress supplied by DaapConfig
     * 
     * @throws IOException
     */
    public void bind() throws IOException {

        SocketAddress bindAddr = config.getInetSocketAddress();
        int backlog = config.getBacklog();

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

            if (LOG.isInfoEnabled()) {
                LOG.info("DaapServerNIO bound to " + bindAddr);
            }

        } catch (IOException err) {
            close();
            throw err;
        }
    }

    protected synchronized void update() {
        update = true;
    }

    /**
     * Stops the DAAP Server
     */
    public synchronized void stop() {
        running = false;
    }

    /**
     * Cloeses the server and releases all resources
     */
    private synchronized void close() {

        running = false;
        update = false;
        disconnectAll = false;

        if (selector != null) {

            for (SelectionKey key : selector.keys())
                cancel(key);

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

        sessionIds.clear();
        connections.clear();
        libraryQueue.clear();
    }

    /**
     * Disconnects all DAAP and Stream connections
     */
    public synchronized void disconnectAll() {
        disconnectAll = true;
    }

    /**
     * Cancel SelesctionKey, close Channel and "free" the attachment
     */
    private void cancel(SelectionKey sk) {

        sk.cancel();

        SelectableChannel channel = sk.channel();

        try {
            channel.close();
        } catch (IOException err) {
            LOG.error("Channel.close()", err);
        }

        DaapConnection connection = (DaapConnection) sk.attachment();

        if (connection != null) {
            closeConnection(connection);
        }
    }

    protected void closeConnection(DaapConnection connection) {
        DaapSession session = connection.getSession(false);
        if (session != null) {
            destroySessionId(session.getSessionId());
        }

        connection.close();

        try {
            removeConnection(connection);
        } catch (IllegalStateException err) {
            // Shouldn't happen
            LOG.error("IllegalStateException", err);
        }
    }

    /**
     * Accept an icoming connection
     * 
     * @throws IOException
     */
    private void processAccept(SelectionKey sk) throws IOException {

        if (!sk.isValid())
            return;

        ServerSocketChannel ssc = (ServerSocketChannel) sk.channel();
        SocketChannel channel = ssc.accept();

        if (channel == null)
            return;

        try {
            Socket socket = channel.socket();
            if (channel.isOpen() && accept(socket.getInetAddress())) {

                channel.configureBlocking(false);

                DaapConnectionNIO connection = new DaapConnectionNIO(this,
                        channel);

                channel.register(selector, SelectionKey.OP_READ, connection);
                addPendingConnection(connection);

            } else {
                channel.close();
            }
        } catch (IOException err) {
            LOG.error("IOException", err);
            try {
                channel.close();
            } catch (IOException iox) {
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

        DaapConnectionNIO connection = (DaapConnectionNIO) sk.attachment();

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

        DaapConnectionNIO connection = (DaapConnectionNIO) sk.attachment();

        boolean keepAlive = false;

        try {
            keepAlive = connection.write();
        } catch (DaapStreamException err) {

            // Broken pipe: User pressed Pause, fast-foward
            // or whatever. Just close the connection and go
            // ahead
            keepAlive = false;
            LOG.error("DaapStreamException", err);
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
    private void processDisconnectAll() {
        for (SelectionKey sk : selector.keys()) {
            SelectableChannel channel = sk.channel();
            if (channel instanceof SocketChannel) {
                cancel(sk);
            }
        }

        libraryQueue.clear();
    }

    /**
     * Notify all clients about an update of the Library
     */
    private void processUpdate() {

        for (DaapConnectionNIO connection : getDaapConnections()) {
            SelectionKey sk = connection.getChannel().keyFor(selector);

            try {

                for (int i = 0; i < libraryQueue.size(); i++) {
                    connection.enqueueLibrary(libraryQueue.get(i));
                }

                connection.update();
                if (sk.isValid()) {
                    try {
                        sk.interestOps(SelectionKey.OP_READ
                                | SelectionKey.OP_WRITE);
                    } catch (CancelledKeyException err) {
                        cancel(sk);
                        LOG.error("SelectionKey.interestOps()", err);
                    }
                }
            } catch (ClosedChannelException err) {
                cancel(sk);
                LOG.error("DaapConnection.update()", err);
            } catch (IOException err) {
                cancel(sk);
                LOG.error("DaapConnection.update()", err);
            }
        }

        libraryQueue.clear();
    }

    /**
     * 1) Disconnect all connections that are in undefined state and that have
     * exceeded their timeout.
     * 
     * 2) Empty the libraryQueue of daap connections if they've exceeded their
     * timeout. Some clients do not support live updates and this will prevent
     * us from running out of memory if the client doesn't fetch its updates).
     */
    protected void processTimeout() {
        for (DaapConnectionNIO connection : getPendingConnections()) {
            if (connection.timeout()) {
                cancelConnection(connection);
            }
        }

        for (DaapConnectionNIO connection : getDaapConnections()) {
            if (connection.timeout()) {
                connection.clearLibraryQueue();
            }
        }
    }

    protected void cancelConnection(DaapConnectionNIO connection) {
        SelectionKey sk = connection.getChannel().keyFor(selector);
        cancel(sk);
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

        while (running) {

            try {
                n = selector.select(TIMEOUT);
            } catch (NullPointerException err) {
                continue;
            } catch (CancelledKeyException err) {
                continue;
            }

            synchronized (this) {

                if (!running) {
                    break;
                }

                if (disconnectAll) {
                    processDisconnectAll();
                    disconnectAll = false;
                    continue; // as all clients were disconnected
                    // there is nothing more to do
                }

                if (update) {
                    processUpdate();
                    update = false;
                }

                if (n > 0) {

                    for (Iterator<SelectionKey> it = selector.selectedKeys()
                            .iterator(); it.hasNext() && running;) {

                        SelectionKey sk = it.next();
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
                                        LOG
                                                .error(
                                                        "An exception occured in processRead()",
                                                        err);
                                    }
                                }

                                if (sk.isWritable()) {
                                    try {
                                        processWrite(sk);
                                    } catch (IOException err) {
                                        cancel(sk);
                                        LOG
                                                .error(
                                                        "An exception occured in processWrite()",
                                                        err);
                                    }
                                }
                            }
                        } catch (CancelledKeyException err) {
                            continue;
                        }
                    }
                }

                // Kill the gremlins
                processTimeout();
            }
        }

        // close() is in finally of run() {}
    }

    /**
     * The run loop
     */
    public void run() {

        try {

            if (running) {
                LOG.error("DaapServerNIO is already running.");
                return;
            }

            selector = Selector.open();

            ssc.register(selector, SelectionKey.OP_ACCEPT);

            process();

        } catch (IOException err) {
            LOG.error("IOException", err);
            throw new RuntimeException(err);

        } finally {
            close();
        }
    }

    /* Make them accessible for classes in this package */
    protected synchronized DaapConnectionNIO getAudioConnection(
            SessionId sessionId) {
        return super.getAudioConnection(sessionId);
    }

    /* Make them accessible for classes in this package */
    protected synchronized DaapConnectionNIO getDaapConnection(
            SessionId sessionId) {
        return super.getDaapConnection(sessionId);
    }

    /* Make them accessible for classes in this package */
    protected synchronized boolean isSessionIdValid(SessionId sessionId) {
        return super.isSessionIdValid(sessionId);
    }

    /* Make them accessible for classes in this package */
    protected synchronized boolean updateConnection(DaapConnectionNIO connection) {
        return super.updateConnection(connection);
    }
}
