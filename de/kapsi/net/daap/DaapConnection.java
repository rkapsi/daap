
package de.kapsi.net.daap;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpParser;
import org.apache.commons.httpclient.HttpStatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	
	public void setAudioStream(boolean audioStream) {
		this.audioStream = audioStream;
	}
	
	public boolean isAudioStream() {
		return audioStream;
	}
	
	public void run() {
    
		try {
			
            do {
                
                server.processRequest(this, getDaapRequest());
                
                // enforce getDaapRequest() to read a new
                // request on the next cycle
                setDaapRequest(null); 
                                    
            } while(--keepAlive > 0 && !audioStream);
			
		} catch (SocketException err) {
			LOG.info(err);
			
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
			Integer revisionNumber = (Integer)session.getAttribute("REVISION-NUMBER");
			
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
    
    private DaapRequest readRequest() 
            throws IOException {
    
		String line = null;
		
		do {
			line = HttpParser.readLine(in);
		} while(line != null && line.length() == 0);
		
		if(line == null) {
            connectionClose();
            throw new IOException();
		}

		DaapRequest request = DaapRequest.parseRequest(line);
		Header[] headers = HttpParser.parseHeaders(in);
		request.setHeaders(headers);
        request.setConfig(server.getConfig());
        
		return request;
    }
}
