
package de.kapsi.net.daap;

import java.io.*;
import java.net.*;
import java.util.*;

import de.kapsi.net.daap.chunks.ServerInfoResponse;
import de.kapsi.net.daap.chunks.ContentCodesResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DaapServer {
	
	private static final Log LOG = LogFactory.getLog(DaapServer.class);
	
	private final static Random generator = new Random();
	
	static {
		for(int i = 0; i < 100; i++) {
			generator.nextInt();
		}
	}
	
    private int threadNo = 0;
    
	private Library library;
	private ServerInfoResponse serverInfo;
	private ContentCodesResponse contentCodes;
	
    private Thread acceptThread;
    private DaapAcceptor acceptor;
    
	private int port;
	private int maxConnections;
    
	private HashSet sessionIds;
	private HashSet connections;
    private HashSet streams;
	
	private DaapRequestHandler requestHandler;
	private DaapAudioRequestHandler audioRequestHandler;
	
	public DaapServer(Library library, int port) 
			throws IOException {
			
		this.library = library;
		this.port = port;
		
		serverInfo = new ServerInfoResponseImpl(library.getName());
		contentCodes = new ContentCodesResponseImpl();
		
		requestHandler = new DaapRequestHandler(serverInfo, contentCodes, library);
		audioRequestHandler = new DaapAudioRequestHandler(library);
        
        sessionIds = new HashSet();
        connections = new HashSet();
        streams = new HashSet();
        
        maxConnections = 1;
	}

	public int getLocalPort() {
		return port;
	}
	
	public void setAuthenticator(DaapAuthenticator authenticator) {
		requestHandler.setAuthenticator(authenticator);
	}
	
	public DaapAuthenticator getAuthenticator() {
		return requestHandler.getAuthenticator();
	}
	
	public void setAudioStream(DaapAudioStream audioStream) {
		audioRequestHandler.setAudioStream(audioStream);
	}
	
	public DaapAudioStream getAudioStream() {
		return audioRequestHandler.getAudioStream();
	}
	
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    
    public int getMaxConnections() {
        return maxConnections;
    }
    
    public boolean isRunning() {
        return (acceptor != null && acceptor.isRunning());
    }
    
    public void start() throws IOException {
        
        if (isRunning())
            return;
        
        acceptor = new DaapAcceptor(this, port, 0, InetAddress.getLocalHost());
        
        acceptThread = new Thread(acceptor, "DaapAcceptorThread");
        acceptThread.start();
    }
    
    public void stop() {
        
        if (!isRunning())
            return;
        
        acceptor.stop();
        
        synchronized(connections) {
            Iterator it = connections.iterator();
            while(it.hasNext()) {
                ((DaapConnection)it.next()).close();
            }
            connections.clear();
        }
        
        synchronized(streams) {
            Iterator it = streams.iterator();
            while(it.hasNext()) {
                ((DaapConnection)it.next()).close();
            }
            streams.clear();
        }
        
        synchronized(sessionIds) {
            sessionIds.clear();
        }
        
        acceptor = null;
        acceptThread = null;
    }
    
    public boolean accept(Socket socket) 
            throws IOException {
        
        DaapConnection connection = new DaapConnection(this, socket);
        
        // 
        int soTimeout = socket.getSoTimeout();
        socket.setSoTimeout(10*1000); // 10 seconds timeout
        DaapRequest request = connection.getDaapRequest();
        socket.setSoTimeout(soTimeout);
        
        if (!request.isSongRequest() && 
                !request.isServerInfoRequest()) {
                
            // disconnect as the first request must be
            // either a song or server-info request!
            return false;
        }
        
        if (request.isSongRequest()) {
            connection.setAudioStream(true);
        }
        
        if (connection.isAudioStream()) {
            
            synchronized(streams) {
                if (streams.size() < maxConnections) {
                    streams.add(connection);
                } else {
                    return false;
                }
            }
            
        } else {
            
            synchronized(connections) {
                
                if (connections.size() < maxConnections) {
                    connection.connectionKeepAlive();
                    connections.add(connection);
                } else {
                
                    // process /server-info (nice exit)
                    // and then close the connection
                    connection.setKeepAlive(-1);
                }
            }
        }
        
        Thread connThread = new Thread(connection, "DaapConnectorThread-" + (++threadNo));
        connThread.setDaemon(true);
        connThread.start();
        
        return true;
    }
    
	public void update() {
        synchronized(connections) {
            Iterator it = connections.iterator();
            while(it.hasNext()) {
                
                DaapConnection conn = (DaapConnection)it.next();
                
                try {
                    conn.update();
                } catch (IOException err) {
                    LOG.error(err);
                }
            }
        }
	}
	
	void processRequest(DaapConnection conn, DaapRequest request) throws IOException {
		
		boolean complete = false;
		
		if (request.isSongRequest()) {
		
			if (isSessionIdValid(new Integer(request.getSessionId()))) {
				audioRequestHandler.processRequest(conn, request);
			}
			
			complete = false; // always disconnect when done
			
		} else {
		
			complete = requestHandler.processRequest(conn, request);
		}
	
        if (!complete) {
            conn.connectionClose();
        }
	}
	
	void removeConnection(DaapConnection conn) {
    
        if (conn.isAudioStream()) {
            
            synchronized(streams) {
                streams.remove(conn);
            }
        
        } else {
            
            synchronized(connections) {
                connections.remove(conn);
            }
            
            DaapSession session = conn.getSession(false);
			if (session != null) {
				session.invalidate();
				
				synchronized(sessionIds) {
					sessionIds.remove(session.getSessionId());
				}
			}
        }
	}
	
	private boolean isSessionIdValid(Integer sessionId) {
		 synchronized(sessionIds) {
			return sessionIds.contains(sessionId);
		 }
	}
	
	/**
	 * Returns an unique session-id
	 */
	Integer createSessionId(DaapConnection conn) {
		
		Integer sessionId = null;
		
		synchronized(sessionIds) {
		
			while(sessionId == null || sessionIds.contains(sessionId)) {
				int tmp = generator.nextInt();
				
				if (tmp == 0) { 
					continue; 
				} else if (tmp < 0) {
					tmp = -tmp;
				}
				
				sessionId = new Integer(tmp);
			}
			
			sessionIds.add(sessionId);
		}
		
		return sessionId;
	}

	public int getNumberOfConnections() {
		return (connections != null) ? connections.size() : 0;
	}
}
