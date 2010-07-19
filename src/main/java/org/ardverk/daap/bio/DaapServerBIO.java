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

package org.ardverk.daap.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.ThreadFactory;

import org.ardverk.daap.DaapConfig;
import org.ardverk.daap.DaapConnection;
import org.ardverk.daap.DaapServer;
import org.ardverk.daap.Library;
import org.ardverk.daap.SessionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This DAAP server is written with the classical I/O and multible Threads.
 * 
 * @author Roger Kapsi
 */
public class DaapServerBIO extends DaapServer<DaapConnectionBIO> {

    private static final Logger LOG 
        = LoggerFactory.getLogger(DaapServerBIO.class);

    private volatile ThreadFactory threadFactory 
        = new DaapThreadFactory("DaapConnectionThread");

    private ServerSocket ssocket;

    /**
     * Creates a new DAAP server with Library and {@see SimpleConfig}
     * 
     * @param library
     *            a Library
     */
    public DaapServerBIO(Library library) {
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
    public DaapServerBIO(Library library, DaapConfig config) {
        super(library, config);
    }

    /**
     * Sets the DaapThreadFactory for this DAAP server
     * 
     * @param fectory
     *            a DaapThreadFactory
     */
    public synchronized void setThreadFactory(ThreadFactory factory) {
        if (factory == null) {
            threadFactory = new DaapThreadFactory("DaapConnectionThread");
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
        if (isRunning())
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
     * Stops the DAAP Server
     */
    public synchronized void stop() {

        if (!isRunning())
            return;

        running = false;

        try {
            if (ssocket != null)
                ssocket.close();
        } catch (IOException err) {
            LOG.error("IOException", err);
        }

        disconnectAll();
    }

    /**
     * Disconnects all DAAP and Stream connections
     */
    public synchronized void disconnectAll() {
        for (DaapConnectionBIO connection : connections) {
            connection.disconnect();
        }

        clear();
    }

    /**
     * Call this to notify the server that Library has changed
     */
    protected synchronized void update() {
        for (DaapConnectionBIO conn : connections) {
            for (Library library : libraryQueue) {
                conn.enqueueLibrary(library);
            }

            try {
                conn.update();
            } catch (IOException err) {
                LOG.error("IOException", err);
            }
        }

        libraryQueue.clear();
    }

    /**
     * The run loop
     */
    @Override
    public void run() {

        running = true;

        try {

            while (running) {
                Socket socket = ssocket.accept();

                try {
                    synchronized (this) {
                        if (running && accept(socket.getInetAddress())) {

                            socket.setSoTimeout(DaapConnection.TIMEOUT);

                            DaapConnectionBIO connection 
                                = new DaapConnectionBIO(this, socket);
                            addPendingConnection(connection);

                            Thread connThread = threadFactory.newThread(connection);
                            connThread.start();

                        } else {
                            socket.close();
                        }
                    }
                } catch (IOException err) {
                    LOG.error("IOException", err);
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    }
                }

                Thread.sleep(100);
            }

        } catch (InterruptedException err) {
            LOG.error("InterruptedException", err);
            // throw new RuntimeException(err);

        } catch (SocketException err) {
            if (running) {
                LOG.error("SocketException", err);
            }
            // throw new RuntimeException(err);

        } catch (IOException err) {
            LOG.error("IOException", err);
            // throw new RuntimeException(err);

        } finally {
            stop();
        }
    }

    /* Make them accessible for classes in this package */
    protected synchronized DaapConnectionBIO getAudioConnection(
            SessionId sessionId) {
        return super.getAudioConnection(sessionId);
    }

    /* Make them accessible for classes in this package */
    protected synchronized DaapConnectionBIO getDaapConnection(
            SessionId sessionId) {
        return super.getDaapConnection(sessionId);
    }

    /* Make them accessible for classes in this package */
    protected synchronized boolean isSessionIdValid(SessionId sessionId) {
        return super.isSessionIdValid(sessionId);
    }

    /* Make them accessible for classes in this package */
    protected synchronized void removeConnection(DaapConnection connection) {
        super.removeConnection(connection);
    }

    /* Make them accessible for classes in this package */
    protected synchronized boolean updateConnection(DaapConnectionBIO connection) {
        if (!isRunning()) {
            return false;
        }

        return super.updateConnection(connection);
    }
}
