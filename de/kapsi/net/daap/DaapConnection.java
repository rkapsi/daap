
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
	
	private boolean keepAlive = false;
	private int requestNumber = 0;
	
	private DaapSession session;
	
	private boolean audioStream;
	
	public DaapConnection(DaapServer server, Socket socket) throws IOException {
		
		this.server = server;
		this.socket = socket;
		
		in = socket.getInputStream();
		out = socket.getOutputStream();
	}
	
	private void setAudioStream(boolean audioStream) {
		this.audioStream = audioStream;
	}
	
	public boolean isAudioStream() {
		return audioStream;
	}
	
	public void run() {
		requestNumber = 0;
		
		try {
			
			requestNumber++;
			DaapRequest request = readRequest();
			
			if (request != null && !request.isUnknownRequest()) {
				
				if (request.isSongRequest()) {
					
					setAudioStream(true);
					server.processRequest(this, request);
					out.flush();
					
				} else {
					
					server.processRequest(this, request);
					out.flush();
			
					do {
					
						keepAlive = false;
						requestNumber++;
						
						request = readRequest();
				
						if (request != null) {
							server.processRequest(this, request);
							out.flush();
						}
				
					} while(keepAlive);
				}
			}
			
		} catch (SocketException err) {
			LOG.info(err);
			
		} catch (IOException err) {
			LOG.error(err);
			
		} finally {
			destroy();
		}
	}
	
	private DaapRequest readRequest() throws IOException {
		
		DaapRequest request = null;
		String line = null;
		
		do {
			line = HttpParser.readLine(in);
		} while(line != null && line.length() == 0);
		
		if(line == null) {
			connectionClose();
			return null;
		}

		
		request = DaapRequest.parseRequest(line);
		Header[] headers = HttpParser.parseHeaders(in);
		request.setHeaders(headers);
			
		return request;
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
					DaapRequest.createUpdateRequest(sessionId.intValue(), revisionNumber.intValue(), delta.intValue());
				
				request.setHeaders(null);
				
				server.processRequest(this, request);
			}
		}
	}
	
	public void connectionKeepAlive() {
		keepAlive = true;
	}
	
	public void connectionClose() {
		keepAlive = false;
	}
	
	void destroy() {
	
		if (socket != null) {
			try {
				socket.close();
				socket = null;
			} catch (IOException err) {
				LOG.error("Error while closing connection", err);
			}
		}
		
		if (session != null) {
			session.invalidate();
		}
		
		server.removeConnection(this);
	}
	
	public ResponseWriter getWriter() {
        try {
			return new ResponseWriter(out);
		} catch (UnsupportedEncodingException err) {
			throw new RuntimeException(err.toString());
		}
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
}
