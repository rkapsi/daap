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

package de.kapsi.net.daap.nio;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import de.kapsi.net.daap.Library;
import de.kapsi.net.daap.DaapRequest;
import de.kapsi.net.daap.DaapResponse;
import de.kapsi.net.daap.DaapSession;
import de.kapsi.net.daap.DaapUtil;

import de.kapsi.net.daap.DaapServer;
import de.kapsi.net.daap.DaapConnection;
import de.kapsi.net.daap.DaapResponseFactory;
import de.kapsi.net.daap.DaapRequestProcessor;
import de.kapsi.net.daap.DaapResponseWriter;

/**
 * A NIO based implementation of DaapConnection.
 *
 * @author  Roger Kapsi
 */
public class DaapConnectionNIO implements DaapConnection {
    
    private static final DaapResponseFactory FACTORY = new DaapResponseFactoryNIO();
    private static final DaapRequestProcessor PROCESSOR = new DaapRequestProcessor(FACTORY);
        
    private DaapServerNIO server;
    private SocketChannel channel;
    
    private DaapRequestReaderNIO reader;
    private DaapResponseWriter writer;
    private DaapSession session;
    
    private int type = DaapConnection.UNDEF;
    private int protocolVersion = DaapUtil.UNDEF_VALUE;
    
    /** Creates a new instance of DaapConnection */
    public DaapConnectionNIO(DaapServerNIO server, SocketChannel channel) {
        this.server = server;
        this.channel = channel;
        
        reader = new DaapRequestReaderNIO(this);
        writer = new DaapResponseWriter();
    }
    
    /**
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
    
    /**
     * Returns <tt>true</tt> if this is an Audio Stream
     *
     * @return
     */    
    public boolean isAudioStream() {
        return (type==DaapConnection.AUDIO);
    }
    
    /**
     * Returns <tt>true</tt> if this is a normal connection
     * 
     * @return
     */    
    public boolean isNormal() {
        return (type==DaapConnection.NORMAL);
    }
    
    /**
     * Returns <tt>true</tt> if this is an indetermined
     * connection
     *
     * @return
     */    
    public boolean isUndef() {
        return (type==DaapConnection.UNDEF);
    }
    
    /**
     *
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }
    
    /**
     * What do you do next?
     *
     * @return
     */    
    public int interrestOps() {
        
        if (isUndef()) {
            return SelectionKey.OP_READ;
        
        } else if (isNormal()) {
            
            int op = SelectionKey.OP_READ;
            
            if (!writer.isEmpty())
                op |= SelectionKey.OP_WRITE;
            
            return op;
        
        } else {
            // isAudioStream
            return SelectionKey.OP_WRITE;
        }
    }
    
    /**
     *
     * @return
     */    
    public DaapServer getServer() {
        return server;
    }
    
    /**
     *
     * @return
     */    
    public SocketChannel getChannel() {
        return channel;
    }
    
    /**
     *
     * @throws IOException
     * @return
     */    
    public boolean read() throws IOException {
        
        if (!isAudioStream()) {
            
            DaapRequest request = reader.read();
            
            if (request != null) {
                
                if (isUndef()) {
                    
                    if (request.isSongRequest()) {
                        type = DaapConnection.AUDIO;
                        
                        // AudioStreams have a session-id and we must check the id
                        Integer sid = new Integer(request.getSessionId());
                        if (server.isSessionIdValid(sid) == false) {
                            throw new IOException("Unknown Session-ID: " + sid);
                        }
                        
                        // Get the associated "normal" connection...
                        DaapConnection connection = server.getConnection(sid);
                        if (connection == null) {
                            throw new IOException("No connection associated with this Session-ID: " + sid);
                        }
                        
                        // ...and use its protocolVersion for this Audio Stream
                        // because Audio Streams do not provide the version in 
                        // the request header (we could use the User-Agent header
                        // but that breaks compatibility to non iTunes hosts and
                        // they would have to fake their request header.
                        protocolVersion = connection.getProtocolVersion();
                        
                    } else if (request.isServerInfoRequest()) {
                        
                        type = DaapConnection.NORMAL;
                        protocolVersion = DaapUtil.getProtocolVersion(request);
                        
                    } else {
                        
                        // disconnect as the first request must be
                        // either a song or server-info request!
                        throw new IOException("Illegal first request: " + request);
                    }
                    
                    if (protocolVersion < DaapUtil.VERSION_2) {
                        throw new IOException("Unsupported Protocol Version: " + protocolVersion);
                    }
                    
                    // All checks passed successfully...
                    server.registerConnection(this);
                }
                
                DaapResponse response = PROCESSOR.process(request);
               
                if (response != null) {
                    writer.add(response);
                }
                
                return true;
            }
            
        }
        
        return false;
    }
    
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
     *
     * @throws IOException
     */    
    public void update() throws IOException {
        
        if (isNormal()) {
            DaapSession session = getSession(false);

            if (session != null) {

                Integer sessionId = session.getSessionId();
                Integer delta = (Integer)session.getAttribute("DELTA");
                Integer revisionNumber = (Integer)session.getAttribute("REVISION-NUMBER");

                if (delta != null && revisionNumber != null) {

                    DaapRequest request =
                        new DaapRequest(this, sessionId.intValue(),
                            revisionNumber.intValue(), delta.intValue());

                    DaapResponse response = PROCESSOR.process(request);

                    if (response != null) {
                        writer.add(response);
                    }
                }
            }
        }
    }
    
    public void close() {
        
        if (session != null) {
            session.invalidate();
        }
        
        session = null;
        reader = null;
        writer = null;
    }
    
    /**
     *
     * @return
     */    
    public String toString() {
        return channel.toString();
    }
}
