
package de.kapsi.net.daap;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpParser;
import org.apache.commons.httpclient.HttpStatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is a cover for an incoming connection. An connection
 * can either be a general DAAP connection or an Audio request.
 */
public class DaapConnection implements Runnable {
    
    private static final Log LOG = LogFactory.getLog(DaapConnection.class);
    
    private DaapServer server;
    
    private Socket socket;
    
    private InputStream in;
    private OutputStream out;
    private ResponseWriter writer;
    
    private int keepAlive = 0;
    
    private DaapSession session;
    private boolean audioStream;
    
    private DaapRequest request;
    
    public DaapConnection(DaapServer server, Socket socket)
            throws IOException {
        
        this.server = server;
        this.socket = socket;
        
        in = socket.getInputStream();
        out = socket.getOutputStream();
        writer = new ResponseWriter(out);
    }
    
    /**
     * Sets if this connection is an Audio stream
     */
    private void setAudioStream(boolean audioStream) {
        this.audioStream = audioStream;
    }
    
    /**
     * Returns true if this connection is an Audio stream
     */
    public boolean isAudioStream() {
        return audioStream;
    }
    
    /**
     * Processes the first Request as we have to know
     * which type of connection this is...
     */
    private boolean init() throws IOException {
        
        DaapRequest request = getDaapRequest();
        
        if (!request.isSongRequest() &&
            !request.isServerInfoRequest()) {
            
            if (LOG.isInfoEnabled()) {
                LOG.info("Illegal first request: " + request);
            }
            
            // disconnect as the first request must be
            // either a song or server-info request!
            return false;
        }
        
        // a connection can be either a song request (a audio stream)
        // or a standart DAAP connection.
        setAudioStream(request.isSongRequest());
        
        // AudioStreams have a session-id and we must check the id
        if (isAudioStream()) {
            
            if (server.isSessionIdValid(request.getSessionId()) == false) {
                return false;
            }
        }
        
        // add connection to the connection pool
        if ( ! server.addConnection(this) ) {
            return false;
        }
        
        return true;
    }
    
    public void run() {
        
        try {
            
            if (init()) {
                
                do {
                    
                    server.processRequest(this, getDaapRequest());
                    
                    // enforce getDaapRequest() to read a new
                    // request on the next cycle
                    setDaapRequest(null);
                    
                } while(--keepAlive > 0 && !audioStream);
            }
            
        } catch (SocketException err) {
            //LOG.info(err);
            
            // This exception can be ignored as it's thrown
            // whenever the user disconnects...
            
        } catch (IOException err) {
            LOG.error(err);
            
        } finally {
            close();
        }
    }
    
    public synchronized void update() throws IOException {
        
        DaapSession session = getSession(false);
        
        // Do not trigger new updates if an update for this connection
        // is already running, it it will autumatically update to the
        // lates revision of the library!
        
        if (session != null && !session.hasAttribute("UPDATE_LOCK")) {
            
            Integer sessionId = session.getSessionId();
            Integer delta = (Integer)session.getAttribute("DELTA");
            Integer revisionNumber 
                = (Integer)session.getAttribute("REVISION-NUMBER");
            
            if (delta != null && revisionNumber != null) {
                
                DaapRequest request =
                    DaapRequest.createUpdateRequest(sessionId.intValue(),
                        revisionNumber.intValue(), delta.intValue());
                
                request.setHeaders(null);
                request.setConfig(server.getConfig());
                
                server.processRequest(this, request);
            }
        }
    }
    
    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }
    
    public void connectionKeepAlive() {
        keepAlive++;
    }
    
    public void connectionClose() {
        keepAlive = 0;
    }
    
    public void close() {
        
        try {
            if (socket != null)
                socket.close();
        } catch (IOException err) {
            LOG.error("Error while closing connection", err);
        }
        
        if (session != null)
            session.invalidate();
        
        server.removeConnection(this);
    }
    
    public ResponseWriter getWriter() {
        return writer;
    }
    
    public DaapSession getSession(boolean create) {
        
        if (session == null && create) {
            Integer sessionId = server.createSessionId(this);
            session = new DaapSession(sessionId);
        }
        
        return session;
    }
    
    public InputStream getInputStream() {
        return in;
    }
    
    public OutputStream getOutputStream() {
        return out;
    }
    
    public void setDaapRequest(DaapRequest request) {
        this.request = request;
    }
    
    public DaapRequest getDaapRequest() throws IOException {
        
        if (request == null)
            request = readRequest();
        
        return request;
    }
    
    private DaapRequest readRequest() throws IOException {
        
        String line = null;
        
        do {
            line = HttpParser.readLine(in);
        } while(line != null && line.length() == 0);
        
        if(line == null) {
            connectionClose();
            throw new IOException("Request is null");
        }
        
        DaapRequest request = DaapRequest.parseRequest(line);
        Header[] headers = HttpParser.parseHeaders(in);
        request.setHeaders(headers);
        request.setConfig(server.getConfig());
        
        return request;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer("DaapConnection [");
        
        buffer.append("Host: ").append(socket.getInetAddress()).append(":")
        .append(socket.getPort());
        
        buffer.append(", audioStream: ").append(isAudioStream());
        buffer.append(", keepAlive: ").append(keepAlive > 0);
        buffer.append(", hasSession: ").append(getSession(false)!=null);
        buffer.append(", lastRequest: ").append(request);
        
        return buffer.append("]").toString();
    }
}
