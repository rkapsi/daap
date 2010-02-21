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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.DaapConfig;
import de.kapsi.net.daap.DaapConnection;
import de.kapsi.net.daap.DaapServer;
import de.kapsi.net.daap.DaapThreadFactory;
import de.kapsi.net.daap.Library;
import de.kapsi.net.daap.SessionId;

/**
 * This DAAP server is written with the classical I/O and multible Threads.
 *
 * @author  Roger Kapsi
 */
public class DaapServerBIO extends DaapServer<DaapConnectionBIO> {
    
    private static final Log LOG = LogFactory.getLog(DaapServerBIO.class);
   
    private int threadNo = 0;
    private DaapThreadFactory threadFactory;
    
    private ServerSocket ssocket;
    
    /**
     * Creates a new DAAP server with Library and {@see SimpleConfig}
     * 
     * @param library a Library
     */  
    public DaapServerBIO(Library library) {
        this(library, new DaapConfig());
    }
    
    /**
     * Creates a new DAAP server with Library and DaapConfig
     * 
     * @param library a Library
     * @param config a DaapConfig
     */  
    public DaapServerBIO(Library library, DaapConfig config) {
        super(library, config);
        
        threadFactory = new DaapThreadFactoryImpl();
    }
    
    /**
     * Sets the DaapThreadFactory for this DAAP server
     * 
     * @param fectory a DaapThreadFactory
     */
    public synchronized void setThreadFactory(DaapThreadFactory factory) {
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
            LOG.error(err);
        }
        
        disconnectAll();
    }
    
    /**
     * Disconnects all DAAP and Stream connections
     */
    public synchronized void disconnectAll() {
        for(DaapConnectionBIO connection : connections) {
            connection.disconnect();
        }
        
        clear();
    }
    
    /**
     * Call this to notify the server that Library has changed
     */
    protected synchronized void update() {
        for(DaapConnectionBIO conn : connections) {
            for(int i = 0; i < libraryQueue.size(); i++) {
                conn.enqueueLibrary(libraryQueue.get(i));
            }
            
            try {
                conn.update();
            } catch (IOException err) {
                LOG.error(err);
            }
        }
        
        libraryQueue.clear();
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
                    synchronized(this) {
                        if (running && accept(socket.getInetAddress())) {
                            
                            socket.setSoTimeout(DaapConnection.TIMEOUT);
                            
                            DaapConnectionBIO connection = new DaapConnectionBIO(this, socket);
                            addPendingConnection(connection);
                            
                            Thread connThread = threadFactory.createDaapThread(connection,
                                    "DaapConnectionThread-" + (++threadNo));
                            connThread.start();
                            
                        } else {
                            socket.close();
                        }
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
    
    /* Make them accessible for classes in this package */
    protected synchronized DaapConnectionBIO getAudioConnection(SessionId sessionId) {
        return super.getAudioConnection(sessionId);
    }
    
    /* Make them accessible for classes in this package */
    protected synchronized DaapConnectionBIO getDaapConnection(SessionId sessionId) {
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
