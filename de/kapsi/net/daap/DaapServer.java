
package de.kapsi.net.daap;

import java.io.IOException;

import de.kapsi.net.daap.chunks.impl.ServerInfoResponse;
import de.kapsi.net.daap.chunks.impl.ContentCodesResponse;

/**
 * The famous DaapServer.
 */
public interface DaapServer extends Runnable {
    
    public Library getLibrary();

    public void setConfig(DaapConfig config);
    
    public void setAuthenticator(DaapAuthenticator authenticator);
    public DaapAuthenticator getAuthenticator();
    
    public void setStreamSource(DaapStreamSource streamSource);
    public DaapStreamSource getStreamSource();
    
    public void setFilter(DaapFilter filter);
    public DaapFilter getFilter();
    
    public DaapConfig getConfig();
    
    public void bind() throws IOException;
    
    public ServerInfoResponse getServerInfoResponse();
    
    public ContentCodesResponse getContentCodesResponse();
    
    /**
     * Returns <tt>true</tt> if DAAP Server
     * accepts incoming connections.
     */
    public boolean isRunning();
    
    /**
     * Stops the DAAP Server
     */
    public void stop();
    
    /**
     * Disconnects all DAAP and Stream connections
     */
    public void disconnectAll();
        
    /**
     * Call this to notify the server that Library has changed
     */
    public void update();
    
    /**
     * Returns the number of connections
     */
    public int getNumberOfConnections();
    
    /**
     * Returns the number of streams
     */
    public int getNumberOfStreams();
    
    /**
     * Returns <tt>true</tt> if sessionId is known and valid
     */
    public boolean isSessionIdValid(Integer sessionId);
    
    /**
     * Returns an unique session-id
     */
    public Integer createSessionId();
}
