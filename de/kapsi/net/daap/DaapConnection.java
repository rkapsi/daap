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
   
    /** Undef type of connection */
    protected static final int UNDEF  = DaapUtil.NULL;
    
    /** A DAAP connection */
    protected static final int DAAP = 1;
    
    /** An audio stream */
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
     * Writes data from the queue to the Channel/OutputStream
     * 
     * @throws IOException
     * @return true if keepAlive
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
     * @return the associated DaapServer
     */    
    public DaapServer getServer() {
        return server;
    }
    
    /**
     * Sets the type of this connection. Either UNDEF, DAAP
     * or an AUDIO stream
     * 
     * @param type the type of this connection
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
     * Returns <code>true</code> if this connection is an audio
     * stream.
     *
     * @return true if this is an audio stream
     */ 
    public boolean isAudioStream() {
        return (type==DaapConnection.AUDIO);
    }
    
    /**
     * Returns <code>true</code> if this connection is a DAAP
     * connection (handles Requests/Respones).
     *
     * @return true if this is a DAAP connection 
     */    
    public boolean isDaapConnection() {
        return (type==DaapConnection.DAAP);
    }
    
    /**
     * Returns <code>true</code> if the type of this connection
     * is currently indetermined.
     * 
     * @return <code>true</code> if connection is indetermined
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
     * @param create if true creates a new Session object if
     *  necessary
     * @return a DaapSession object or null if create is false
     *  and no DaapSession object existed
     */    
    public DaapSession getSession(boolean create) {
        
        if (session == null && create) {
            session = new DaapSession(server.createSessionId());
        }
        
        return session;
    }
}
