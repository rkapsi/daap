/*
 * DaapConnection.java
 *
 * Created on April 5, 2004, 6:48 PM
 */

package de.kapsi.net.daap;

import java.io.IOException;

/**
 * 
 * @author  roger
 */
public interface DaapConnection {
   
    static final int UNDEF  = 0;
    static final int NORMAL = 1;
    static final int AUDIO  = 2;
    
    /**
     * 
     * @throws IOException
     */    
    public void update() throws IOException;
    
    /**
     * 
     * @param create
     * @return
     */    
    public DaapSession getSession(boolean create);
    
    /**
     * Returns the DaapServer
     * @return
     */    
    public DaapServer getServer();
    
    /**
     * Returns <tt>true</tt> if this connection is an audio
     * stream.
     *
     * @return
     */    
    public boolean isAudioStream();
    
    /**
     * Returns <tt>true</tt> if this connection is a normal
     * connection (handles Requests/Respones).
     *
     * @return
     */    
    public boolean isNormal();
    
    /**
     * Returns <tt>true</tt> if the type of this connection
     * is currently indetermined.
     * 
     * @return <tt>true</tt> if connection is indetermined
     */    
    public boolean isUndef();
    
    /**
     * Returns <tt>true</tt> if the type of this connection
     * is currently indetermined.
     * 
     * @return <tt>true</tt> if connection is indetermined
     */
    public int getProtocolVersion();
    
    /**
     * Called when a DaapConnection is beeing removed
     * from the connection pool.
     */
    public void close();
}
