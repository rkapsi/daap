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

package de.kapsi.net.daap;

import java.io.IOException;

/**
 * Interface of the DaapServer
 *
 * @author  Roger Kapsi
 */
public interface DaapServer extends Runnable {
    
    /**
     * Returns the Library of this server
     * 
     * @return Library
     */  
    public Library getLibrary();

    /**
     * Sets the DaapConfig for this server
     * 
     * @param config DaapConfig
     */
    public void setConfig(DaapConfig config);
    
    /**
     * Returns the DaapConfig of this server
     * 
     * @return DaapConfig of this server
     */    
    public DaapConfig getConfig();
    
    /**
     * Sets the DaapAuthenticator for this server
     * 
     * @param authenticator a DaapAuthenticator
     */   
    public void setAuthenticator(DaapAuthenticator authenticator);
    
    /**
     * Retrieves the DaapAuthenticator of this server
     * 
     * @return DaapAuthenticator or <code>null</code>
     */   
    public DaapAuthenticator getAuthenticator();
    
    /**
     * Sets the DaapStreamSource for this server
     * 
     * @param streamSource a DaapStreamSource
     */  
    public void setStreamSource(DaapStreamSource streamSource);
    
    
    /**
     * Retrieves the DaapStreamSource of this server
     * 
     * @return DaapStreamSource or <code>null</code>
     */  
    public DaapStreamSource getStreamSource();
    
    /**
     * Sets a DaapFilter for this server
     * 
     * @param filter a DaapFilter
     */  
    public void setFilter(DaapFilter filter);
    
    /**
     * Returns a DaapFilter
     * 
     * @return a DaapFilter or <code>null</code>
     */   
    public DaapFilter getFilter();
    
    /**
     * Sets the factory for Threads. Servers (NIO) that do not 
     * support a Threaded moddel throw an {@see java.lang.UnsupportedOperationException}
     * 
     * @param factory a DaapThreadFactory
     */
    public void setThreadFactory(DaapThreadFactory factory);
    
    /**
     * Binds this server to the SocketAddress supplied by DaapConfig
     * 
     * @throws IOException
     */ 
    public void bind() throws IOException;
    
    /**
     * Returns <code>true</code> if the server is running.
     * 
     * @return <code>true</code> if the server is running
     */
    public boolean isRunning();
    
    /**
     * Stops the DAAP Server
     */
    public void stop();
    
    /**
     * Disconnects all from the server
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
     * Returns <code>true</code> if sessionId is valid
     */
    public boolean isSessionIdValid(Integer sessionId);
    
    /**
     * Creates and returns an unique session-id
     */
    public Integer createSessionId();
    
    /**
     * Returns a DAAP connection from the "normal" connections
     * pool (i.e. a non audio stream) for the sessionId. The
     * primary purpose for this method is that Audio Streams
     * can determinate their "DAAP" connection
     */
    DaapConnection getConnection(Integer sessionId);
}
