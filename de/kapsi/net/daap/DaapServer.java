
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
public class DaapServer {
	
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
	
    private DaapAcceptor acceptor;
    private DaapFilter filter;
    
	private HashSet sessionIds;
	private HashSet connections;
    private HashSet streams;
	
    private DaapConfig config;
    
	private DaapRequestHandler requestHandler;
	private DaapAudioRequestHandler audioRequestHandler;
	
    public DaapServer(Library library) {
        this(library, new SimpleConfig());
    }
    
    public DaapServer(Library library, int port) {
        this(library, new SimpleConfig(port));
    }
    
	public DaapServer(Library library, DaapConfig config) {
			
		this.library = library;
		this.config = config;
        
		serverInfo = new ServerInfoResponseImpl(library.getName());
		contentCodes = new ContentCodesResponseImpl();
		
		requestHandler = new DaapRequestHandler(serverInfo, contentCodes, library);
		audioRequestHandler = new DaapAudioRequestHandler(library);
        
        sessionIds = new HashSet();
        connections = new HashSet();
        streams = new HashSet();
	}
    
    public void setConfig(DaapConfig config) {
        this.config = config;
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
    
    public void setFilter(DaapFilter filter) {
        this.filter = filter;
    }
    
    public DaapFilter getFilter() {
        return filter;
    }

    public DaapConfig getConfig() {
        return config;
    }
    
    /**
     * Returns <tt>true</tt> if DAAP Server
     * accepts incoming connections.
     */
    public synchronized boolean isRunning() {
        if (acceptor == null)
            return false;
        return acceptor.isRunning();
    }
    
    /**
     * Starts the DAAP Server
     */
    public synchronized void start() throws IOException {
        
        if (isRunning())
            return;
        
        threadNo = 0;
        
        acceptor = new DaapAcceptor(this);
        
        Thread acceptThread = new Thread(acceptor, "DaapAcceptorThread");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }
    
    /**
     * Stops the DAAP Server
     */
    public synchronized void stop() {
        
        if (!isRunning())
            return;
        
        acceptor.stop();
        acceptor = null;
        
        disconnectAll();
    }
    
    /**
     * Disconnects all DAAP and Stream connections
     */
    public synchronized void disconnectAll() {
        
        if (!isRunning())
            return;
            
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
    
    /**
     * Adds connection to the internal connection pool and returns true
     * on success. False is retuned in the following cases: Max connections
     * reached or server is down.
     */
    private synchronized boolean addConnection(DaapConnection connection) {
        
        if (!isRunning()) {
            
            if (LOG.isInfoEnabled()) {
                LOG.info("Server is down.");
            }
                    
            return false;
        }
        
        if (connection.isAudioStream()) {
            
            synchronized(streams) {
                
                if (streams.size() < config.getMaxConnections()) {
                    streams.add(connection);
                    
                } else {
                    
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Connection limit reached");
                    }
                    
                    return false;
                }
            }
            
        } else {
            
            synchronized(connections) {
                
                if (connections.size() < config.getMaxConnections()) {
                    connection.connectionKeepAlive();
                    connections.add(connection);
                    
                } else {
                
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Connection limit reached");
                    }
                    
                    // This allows us to disconnect iTunes silently. We
                    // process the first request (/server-info) and don't
                    // keep the connection alive. An alternative would be
                    // to 'return false' but iTunes displays a misleading
                    // error dialog then... 
                    // TODO: someone has to check which of these two options 
                    // is the default behaviour)
                    
                    //connection.setKeepAlive(-1); // silent disconnect
                    
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Called by DaapAcceptor
     */
    boolean accept(Socket socket) 
            throws IOException {
        
        
        if (filter != null && 
                filter.accept(socket.getInetAddress()) == false) {
            
            if (LOG.isInfoEnabled()) {
                LOG.info("DaapFilter refused connection from " + socket);
            }
            
            return false;
        }

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
            
            if (LOG.isInfoEnabled()) {
                LOG.info("Illegal request: " + request);
            }
            
            // disconnect as the first request must be
            // either a song or server-info request!
            return false;
        }
        
        // a connection can be either a song request (a audio stream)
        // or a standart DAAP connection. 
        connection.setAudioStream(request.isSongRequest());
        
        // AudioStreams have a session-id and we must check the id
        if (connection.isAudioStream()) {
            //System.out.println(request);
            if (isSessionIdValid(request.getSessionId()) == false) {
                return false;
            }
        }
        
        // add connection to the connection pool
        if ( ! addConnection(connection) ) {
            return false;
        }
        
        Thread connThread = new Thread(connection, "DaapConnectionThread-" + (++threadNo));
        connThread.setDaemon(true);
        connThread.start();
        
        return true;
    }
    
    /**
     * Call this to notify the server that Library has changed
     */
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
	
    /**
     *
     */
	void processRequest(DaapConnection conn, DaapRequest request) 
            throws IOException {
		
		boolean complete = false;
		
		if (request.isSongRequest()) {
		
			if (isSessionIdValid(request.getSessionId())) {
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
	
    /**
     *
     */
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
	
    /**
     * Returns <tt>true</tt> if sessionId is known and valid
     */
    private boolean isSessionIdValid(int sessionId) {
		 return isSessionIdValid(new Integer(sessionId));
	}
    
    /**
     * Returns <tt>true</tt> if sessionId is known and valid
     */
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
    
    /**
     * Returns the number of connections
     */
	public int getNumberOfConnections() {
        if (connections == null)
            return 0;
        
        synchronized(connections) {
            return connections.size();
        }
	}
    
    /**
     * Returns the number of streams
     */
    public int getNumberOfStreams() {
        if (streams == null)
            return 0;
        
        synchronized(streams) {
            return streams.size();
        }
    }
}
