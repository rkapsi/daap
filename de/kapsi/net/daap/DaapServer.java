
package de.kapsi.net.daap;

import java.io.IOException;

import de.kapsi.net.daap.chunks.impl.ServerInfoResponse;
import de.kapsi.net.daap.chunks.impl.ContentCodesResponse;

/**
 * Interface of the DaapServer
 */
public interface DaapServer extends Runnable {
    
    /**
     *
     * @return
     */    
    public Library getLibrary();

    /**
     *
     * @param config
     */    
    public void setConfig(DaapConfig config);
    
    /**
     *
     * @param authenticator
     */    
    public void setAuthenticator(DaapAuthenticator authenticator);
    /**
     *
     * @return
     */    
    public DaapAuthenticator getAuthenticator();
    
    /**
     *
     * @param streamSource
     */    
    public void setStreamSource(DaapStreamSource streamSource);
    /**
     *
     * @return
     */    
    public DaapStreamSource getStreamSource();
    
    /**
     *
     * @param filter
     */    
    public void setFilter(DaapFilter filter);
    /**
     *
     * @return
     */    
    public DaapFilter getFilter();
    
    /**
     *
     * @param factory
     */    
    public void setThreadFactory(DaapThreadFactory factory);
    
    /**
     *
     * @return
     */    
    public DaapConfig getConfig();
    
    /**
     *
     * @throws IOException
     */    
    public void bind() throws IOException;
    
    /**
     *
     * @return <tt>true</tt> if DAAP Server is running
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
     * Returns <tt>true</tt> if sessionId is valid
     */
    public boolean isSessionIdValid(Integer sessionId);
    
    /**
     * Creates and returns an unique session-id
     */
    public Integer createSessionId();
}
