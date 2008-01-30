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

package de.kapsi.net.daap.bio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpException;

import de.kapsi.net.daap.DaapConnection;
import de.kapsi.net.daap.DaapRequest;
import de.kapsi.net.daap.DaapRequestProcessor;
import de.kapsi.net.daap.DaapResponse;
import de.kapsi.net.daap.DaapResponseFactory;
import de.kapsi.net.daap.DaapSession;
import de.kapsi.net.daap.DaapStreamException;
import de.kapsi.net.daap.DaapUtil;
import de.kapsi.net.daap.SessionId;

/**
 * This class is a cover for an incoming connection and is based 
 * on the classical BIO (Blocking I/O) pattern. A connection
 * can either be a general DAAP connection or an Audio request.
 *
 * @author  Roger Kapsi
 */
public class DaapConnectionBIO extends DaapConnection implements Runnable {
    
    private static final Log LOG = LogFactory.getLog(DaapConnectionBIO.class);
    
    private static final DaapResponseFactory FACTORY = new DaapResponseFactoryBIO();
    private static final DaapRequestProcessor PROCESSOR = new DaapRequestProcessor(FACTORY);
    
    private Socket socket;
    
    private InputStream in;
    private OutputStream out;
    
    private boolean running = false;
    
    public DaapConnectionBIO(DaapServerBIO server, Socket socket) throws IOException {
        super(server);
        
        this.socket = socket;
        
        in = new BufferedInputStream(socket.getInputStream());
        out = socket.getOutputStream();
        
        running = true;
    }
    
    private boolean read() throws IOException {
        
        DaapRequest request = readRequest();
            
        if (!isAudioStream()) {
            
            if (isUndef()) {
                
                if (request.isSongRequest()) {
                    setConnectionType(ConnectionType.AUDIO);
                    
                    // as it is an audio connection there's nothing more to read
                    socket.shutdownInput();
                    
                    // AudioStreams have a session-id and we must check the id
                    SessionId sid = request.getSessionId();
                    if (((DaapServerBIO)server).isSessionIdValid(sid) == false) {
                        throw new IOException("Unknown Session-ID: " + sid);
                    }
                    
                    // Get the associated "normal" connection...
                    DaapConnection connection = ((DaapServerBIO)server).getDaapConnection(sid);
                    if (connection == null) {
                        throw new IOException("No connection associated with this Session-ID: " + sid);
                    }
                    
                    // ... and check if there's already an audio connection
                    DaapConnection audio = ((DaapServerBIO)server).getAudioConnection(sid);
                    if (audio != null) {
                        throw new IOException("Multiple audio connections not allowed: " + sid);
                    }
                    
                    // ...and use its protocolVersion for this Audio Stream
                    // because Audio Streams do not provide the version in 
                    // the request header (we could use the User-Agent header
                    // but that breaks compatibility to non iTunes hosts and
                    // they would have to fake their request header.
                    setProtocolVersion(connection.getProtocolVersion());
                        
                } else if (request.isServerInfoRequest()) {
                    setConnectionType(ConnectionType.DAAP);
                    setProtocolVersion(DaapUtil.getProtocolVersion(request));
                    
                } else {
                    
                    // disconnect as the first request must be
                    // either a song or server-info request!
                    throw new IOException("Illegal first request: " + request);
                }
                
                if (!DaapUtil.isSupportedProtocolVersion(getProtocolVersion())) {
                    throw new IOException("Unsupported Protocol Version: " + getProtocolVersion());
                }
                
                // add connection to the connection pool
                if ( ! ((DaapServerBIO)server).updateConnection(this) ) {
                    throw new IOException("Too many connections");
                }
                
                // see run()
                socket.setSoTimeout(LIBRARY_TIMEOUT);
            }

            DaapResponse response = PROCESSOR.process(request);
            if (response != null) {
                writer.add(response);
            }
            
            return true;
        }
        
        throw new IOException("Cannot read requests from audio stream");
    }

    public void run() {
        
        try {
            
            do {
                try {
                    read();
                } catch (SocketTimeoutException err) {
                    if (isDaapConnection() && err.bytesTransferred == 0) {
                        // Some clients do not support live updates and
                        // this will prevent us from running out of memory. 
                        clearLibraryQueue();
                    } else {
                        throw err;
                    }
                }
            } while(running && write());
           
        } catch (DaapStreamException err) {
            
            // LOG.info(err);
            // This exception can be ignored as it's thrown
            // whenever the user presses the pause, fast-forward
            // and so on button
            
        } catch (SocketException err) {
            
            // LOG.info(err);
            // This exception can be ignored as it's thrown
            // whenever the user disconnects...
            
        } catch (IOException err) {
            LOG.error(err);
        } finally {
            close();
        }
    }
    
    public synchronized void update() throws IOException {
        
        if (isDaapConnection() && !isLocked()) {
            DaapSession session = getSession(false);
            if (session != null) {
                SessionId sessionId = session.getSessionId();
                
                // client's revision
                //Integer delta = new Integer(getFirstInQueue().getRevision());
                Integer delta = (Integer)session.getAttribute("CLIENT_REVISION");
                
                // to request
                Integer revisionNumber = new Integer(getFirstInQueue().getRevision());
                
                DaapRequest request =
                    new DaapRequest(this, sessionId,
                        revisionNumber.intValue(), delta.intValue());

                DaapResponse response = PROCESSOR.process(request);

                if (response != null) {
                    response.write();
                }
            }
        }
    }
    
    protected synchronized void disconnect() {
        running = false;
        close();
    }
    
    public synchronized void close() {
        try {
            super.close();
            
            try {
                if (in != null)
                    in.close();
            } catch (IOException err) {
                LOG.error("Error while closing connection", err);
            }
            
            try {
                if (out != null)
                    out.close();
            } catch (IOException err) {
                LOG.error("Error while closing connection", err);
            }
            
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException err) {
                LOG.error("Error while closing connection", err);
            }
        } finally {
            
            // running is true if thread died (e.g. due to an IOE)
            // and it's false if disconnect() was called. The latter
            // case would cause a ConcurrentModificationException in
            // DaapServerBIO#disconnectAll() if we'd remove 'this'
            // in both cases.
            
            if (running) {
                ((DaapServerBIO)server).removeConnection(this);
            }
        }
    }
    
    protected InputStream getInputStream() {
        return in;
    }
    
    protected OutputStream getOutputStream() {
        return out;
    }
    
    private DaapRequest readRequest() throws IOException {
        
        String line = null;
        
        do {
            line = HttpParser.readLine(in);
        } while(line != null && line.length() == 0);
        
        if(line == null) {
            throw new IOException("Request is null: " + this);
        }

        DaapRequest request = null;
        try {
            request = new DaapRequest(this, line);        
            Header[] headers = HttpParser.parseHeaders(in);
            request.addHeaders(headers);            
            return request;
        } catch (URISyntaxException e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        } catch (HttpException e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer("DaapConnection [");
        
        buffer.append("Host: ").append(socket.getInetAddress()).append(":")
        .append(socket.getPort());
        
        buffer.append(", audioStream: ").append(isAudioStream());
        buffer.append(", hasSession: ").append(getSession(false)!=null);
        
        return buffer.append("]").toString();
    }
}
