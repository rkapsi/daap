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
 * An abstract base class for DaapConnections.
 *
 * @author  Roger Kapsi
 */
public abstract class DaapConnection {
   
    protected static final int UNDEF  = DaapUtil.UNDEF_VALUE;
    protected static final int NORMAL = 1;
    protected static final int AUDIO  = 2;
    
    protected final DaapResponseWriter writer;
    
    private DaapServer server;
    private DaapSession session;
    
    private int type = UNDEF;
    private int protocolVersion = UNDEF;

    public DaapConnection(DaapServer server) {
        this.server = server;
        writer = new DaapResponseWriter();
    }
    
    /**
     * Call this to initiate an update which causes the 
     * client to update to the current Library revision.
     *
     * @throws IOException
     */    
    public abstract void update() throws IOException;
    
    /**
     *
     * @throws IOException
     * @return
     */ 
    public boolean write() throws IOException {
        
        if (writer.write()) {

            if (isAudioStream()) {
                return false;
            }
        }

        return true;
    }
    
    /**
     * Called when a DaapConnection is beeing removed
     * from the connection pool.
     */
    public void close() {
        
        writer.clear();
        
        if (session != null) {
            session.invalidate();
            session = null;
        }
    }
    
    /**
     * Returns the DaapServer to which this DaapConnection is
     * associated to.
     *
     * Returns the DaapServer
     * @return
     */    
    public DaapServer getServer() {
        return server;
    }
    
    /**
     * Sets the type of this connection
     */
    protected void setConnectionType(int type) {
        this.type = type;
    }
    
    /**
     * Sets the protocol version of this connection
     */
    protected void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    
    /**
     * Returns <tt>true</tt> if this connection is an audio
     * stream.
     *
     * @return
     */ 
    public boolean isAudioStream() {
        return (type==DaapConnection.AUDIO);
    }
    
    /**
     * Returns <tt>true</tt> if this connection is a normal
     * connection (handles Requests/Respones).
     *
     * @return
     */    
    public boolean isNormal() {
        return (type==DaapConnection.NORMAL);
    }
    
    /**
     * Returns <tt>true</tt> if the type of this connection
     * is currently indetermined.
     * 
     * @return <tt>true</tt> if connection is indetermined
     */    
    public boolean isUndef() {
        return (type==DaapConnection.UNDEF);
    }
    
    /**
     * Returns the protocol version of this connection or
     * DaapUtil.UNDEF_VALUE if it is currently unknown
     * 
     * @return protocol version of this connection
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }
    
    /**
     * Creates if nessesary a new DaapSession and returns it.
     *
     * @param create
     * @return
     */    
    public DaapSession getSession(boolean create) {
        
        if (session == null && create) {
            session = new DaapSession(server.createSessionId());
        }
        
        return session;
    }
}
