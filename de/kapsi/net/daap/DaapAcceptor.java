
package de.kapsi.net.daap;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Accepts incoming connections
 */
public class DaapAcceptor implements Runnable {
    
    private static final Log LOG = LogFactory.getLog(DaapAcceptor.class);
    
    private DaapServer server;
    private ServerSocket ssocket;
    
    private boolean running = false;
    
    public DaapAcceptor(DaapServer server) 
            throws IOException {
            
        this.server = server;
        
        DaapConfig config = server.getConfig();
        
        int port = config.getPort();
        int backlog = config.getBacklog();
        InetAddress bindAddr = config.getBindAddress();
        
        ssocket = new ServerSocket(port, backlog, bindAddr);
        
        if (LOG.isInfoEnabled()) {
            if (bindAddr == null) {
                LOG.info("New DaapServer bound to port: " + port);
            } else {
                LOG.info("New DaapServer bound to " + bindAddr + ":" + port);
            }
        }
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public void stop() {
        if (!running)
            return;
            
        running = false;
        
        try {
            ssocket.close();
        } catch (IOException err) {}
    }

    public void run() {
        
        running = true;
        
        try {
            
            while(running) {
                Socket socket = ssocket.accept();
                
                try {
                    
                    if ( ! server.accept(socket) ) {
                        
                        if (LOG.isInfoEnabled()) {
                            LOG.info("DaapServer refused incoming connection " + socket);
                        }
                        
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
