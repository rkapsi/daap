
package de.kapsi.net.daap;

import java.io.*;
import java.net.*;
import java.util.*;

import de.kapsi.net.daap.chunks.ServerInfoResponse;
import de.kapsi.net.daap.chunks.ContentCodesResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The famous DaapServer.
 */
public class DaapServer implements DaapConfig {
	
	private static final Log LOG = LogFactory.getLog(DaapServer.class);
	
	private final static Random generator = new Random();
	
	static {
        // warm up...
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
    
	public int getPort() {
		return port;
	}
	
    public int getBacklog() {
        return 0;
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
    
    public String getServerName() {
        return "iTunes/4.2 (Mac OS X)";
    }
    
    public DaapConfig getConfig() {
        return this;
    }
    
    public boolean isRunning() {
        return (acceptor != null && acceptor.isRunning());
    }
    
    public void start() throws IOException {
        
        if (isRunning())
            return;
        
        threadNo = 0;
        
        acceptor = new DaapAcceptor(this, port, 0, InetAddress.getLocalHost());
        
        acceptThread = new Thread(acceptor, "DaapAcceptorThread");
        acceptThread.start();
    }
    
    public void stop() {
        
        if (!isRunning())
            return;
        
        acceptor.stop();
        
        acceptor = null;
        acceptThread = null;
        
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
    }
    
    public boolean accept(Socket socket) 
            throws IOException {
        
        DaapConnection connection = new DaapConnection(this, socket);
        
        // Set the timeout to an acceptable value and read the first request.
        // This is necessary as we have to know which type of connection this 
        // is and we don't want block the DaapAcceptorThread for too long. We 
        // could start a DaapConnectionThread immediately but that conflicts
        // with the Library/DaapServer update logic and would need more logic
        // and processsing resources to make sure so that don't issue an update
        // on an uninitialized connection etc...
        
        int oldTimeout = socket.getSoTimeout();
        socket.setSoTimeout(10*1000); // 10 seconds timeout
        DaapRequest request = connection.getDaapRequest();
        socket.setSoTimeout(oldTimeout);
        
        if (!request.isSongRequest() && 
                !request.isServerInfoRequest()) {
                
            // disconnect as the first request must be
            // either a song or server-info request!
            return false;
        }
        
        // a connection can be either a song request (a audio stream)
        // or a standart DAAP connection. 
        connection.setAudioStream(request.isSongRequest());
        
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
                
                    // This allows us to disconnect iTunes silently. We
                    // process the first request (/server-info) and don't
                    // keep the connection alive. An alternative would be
                    // to 'return false' but iTunes displays a misleading
                    // error dialog then... (TODO: someone has to check
                    // which of these two options is the default behaviour)
                    connection.setKeepAlive(-1);
                }
            }
        }
        
        Thread connThread = new Thread(connection, "DaapConnectionThread-" + (++threadNo));
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
