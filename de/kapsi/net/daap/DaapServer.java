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

import de.kapsi.net.daap.chunks.impl.ServerInfoResponse;
import de.kapsi.net.daap.chunks.impl.ContentCodesResponse;

/**
 * Interface of the DaapServer
 *
 * @author  Roger Kapsi
 */
public interface DaapServer extends Runnable {
    
    /**
     * Returns the assocciated Library
     * 
     * @return
     */    
    public Library getLibrary();

    /**
     * Sets the DaapConfig (note: has no effect if DaapServer
     * is running)
     *
     * @param config
     */    
    public void setConfig(DaapConfig config);
    
    /**
     * Sets the DaapAuthenticator (note: use <tt>null</tt> null to
     * disable the authenticator).
     *
     * @param authenticator
     */    
    public void setAuthenticator(DaapAuthenticator authenticator);
    
    /**
     * Returns the DaapAuthenticator or <tt>null</tt> if 
     * no authenticator is set.
     *
     * @return
     */    
    public DaapAuthenticator getAuthenticator();
    
    /**
     * Sets the DaapStreamSource which maps the actual files
     * to Songs.
     *
     * @param streamSource
     */    
    public void setStreamSource(DaapStreamSource streamSource);
    
    
    /**
     * Returns the DaapStreamSource
     *
     * @return
     */    
    public DaapStreamSource getStreamSource();
    
    /**
     * Sets the DaapFilter.
     *
     * @param filter
     */    
    public void setFilter(DaapFilter filter);
    
    /**
     * Returns the DaapFilter.
     *
     * @return
     */    
    public DaapFilter getFilter();
    
    /**
     * A DaapThreadFactory enables you to create customized 
     * Threads from outside of DaapServer (optional for the 
     * BIO server!).
     *  
     * @param factory
     */    
    public void setThreadFactory(DaapThreadFactory factory);
    
    /**
     * Returns the DaapConfig.
     * @return
     */    
    public DaapConfig getConfig();
    
    /**
     * Binds the DaapServer.
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
    
    /**
     * Returns a DAAP connection from the "normal" connections
     * pool (i.e. a non audio stream) for the sessionId. The
     * primary purpose for this method is that Audio Streams
     * can determinate their "normal" connection
     */
    DaapConnection getConnection(Integer sessionId);
}
