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
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.DaapConnection;
import de.kapsi.net.daap.DaapRequest;
import de.kapsi.net.daap.DaapRequestProcessor;
import de.kapsi.net.daap.DaapResponse;
import de.kapsi.net.daap.DaapResponseFactory;
import de.kapsi.net.daap.DaapSession;
import de.kapsi.net.daap.DaapUtil;

/**
 * A NIO based implementation of DaapConnection.
 *
 * @author  Roger Kapsi
 */
public class DaapConnectionNIO extends DaapConnection {
    
    private static final Log LOG = LogFactory.getLog(DaapConnectionNIO.class);
    
    private static final DaapResponseFactory FACTORY = new DaapResponseFactoryNIO();
    private static final DaapRequestProcessor PROCESSOR = new DaapRequestProcessor(FACTORY);
     
    private DaapServerNIO server;
    private SocketChannel channel;
    private DaapRequestReaderNIO reader;
    
    /** Creates a new instance of DaapConnection */
    public DaapConnectionNIO(DaapServerNIO server, SocketChannel channel) {
        super(server);
        
        this.server = server;
        this.channel = channel;
        
        reader = new DaapRequestReaderNIO(this);
    }
    
    /**
     * What do you do next?
     *
     * @return
     */    
    int interrestOps() {
        
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
                        setConnectionType(DaapConnection.AUDIO);
                        
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
                        setProtocolVersion(connection.getProtocolVersion());
                        
                    } else if (request.isServerInfoRequest()) {
                        
                        setConnectionType(DaapConnection.NORMAL);
                        setProtocolVersion(DaapUtil.getProtocolVersion(request));
                        
                    } else {
                        
                        // disconnect as the first request must be
                        // either a song or server-info request!
                        throw new IOException("Illegal first request: " + request);
                    }
                    
                    if (!DaapUtil.isSupportedProtocolVersion(getProtocolVersion())) {
                        throw new IOException("Unsupported Protocol Version: " + getProtocolVersion());
                    }
                    
                    // All checks passed successfully...
                    server.registerConnection(this);
                }
                
                DaapResponse response = PROCESSOR.process(request);
                
                if (LOG.isTraceEnabled()) {
                    LOG.trace(request);
                    LOG.trace(response);
                }
                
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
     */    
    public void update() throws IOException {
        
        if (isNormal()) {
            DaapSession session = getSession(false);

            if (session != null && !session.hasAttribute("UPDATE_LOCK")) {

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
        super.close();
        reader = null;
    }
    
    /**
     *
     * @return
     */    
    public String toString() {
        return channel.toString();
    }
}
