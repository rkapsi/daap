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
 * An interface for DaapConnections.
 *
 * @author  Roger Kapsi
 */
public interface DaapConnection {
   
    static final int UNDEF  = 0;
    static final int NORMAL = 1;
    static final int AUDIO  = 2;
    
    /**
     * Call this to initiate an update which causes the 
     * client to update to the current Library revision.
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
     * Returns the DaapServer to which this DaapConnection is
     * associated to.
     *
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
     * Returns the protocol version of this connection or
     * DaapUtil.UNDEF_VALUE if it is currently unknown
     * 
     * @return protocol version of this connection
     */
    public int getProtocolVersion();
    
    /**
     * Called when a DaapConnection is beeing removed
     * from the connection pool.
     */
    public void close();
}
