
package de.kapsi.net.daap;

import java.io.*;
import java.net.*;
import java.util.*;

import de.kapsi.net.daap.chunks.ServerInfoResponse;
import de.kapsi.net.daap.chunks.ContentCodesResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DaapServer implements Runnable {
	
	private static final Log LOG = LogFactory.getLog(DaapServer.class);
	
	private final static Random generator = new Random();
	
	static {
		for(int i = 0; i < 100; i++) {
			generator.nextInt();
		}
	}
	
	private boolean serve = false;
	
	private Library library;
	private ServerInfoResponse serverInfo;
	private ContentCodesResponse contentCodes;
	
	private int port;
	private ServerSocket serverSocket;
	
	private HashSet sessionIds;
	private HashSet connections;
	
	private Thread acceptThread;
	private ThreadGroup group;
	
	private DaapRequestHandler requestHandler;
	private DaapAudioRequestHandler audioRequestHandler;
	
	public DaapServer(Library library, int port) 
			throws IOException {
			
		this.library = library;
		this.port = port;
		
		serverInfo = new ServerInfoResponseImpl(library.getName());
		contentCodes = new ContentCodesResponseImpl();
	
		this.group = new ThreadGroup("DaapServer Thread Group");
		
		requestHandler = new DaapRequestHandler(serverInfo, contentCodes, library);
		audioRequestHandler = new DaapAudioRequestHandler(library);
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
	
	public void run() {
		
		try {
			
			serverSocket = new ServerSocket(port);
			
			if (LOG.isInfoEnabled()) {
				LOG.info("New DaapServer bound to port: " + port);
			}
			
			sessionIds = new HashSet();
			connections = new HashSet();
			
			while(serve && !Thread.interrupted()) {
			
				Socket socket = serverSocket.accept();
				socket.setSoTimeout(1800*1000); 
                
				DaapConnection conn = new DaapConnection(this, socket);
				
				synchronized(connections) {
					connections.add(conn);
				}

				Thread connThread = new Thread(group, conn, "DaapConnection");
				connThread.setDaemon(true);
				connThread.start();
				
				Thread.sleep(100);
			}
			
		} catch (InterruptedException err) {
        } catch (SocketException err) {
            
			if (!serve) {
                LOG.error("DaapServer error", err);
                throw new RuntimeException(err.getMessage());
            }
			
        } catch (IOException err) {
            
			LOG.error("DaapServer error", err);
            throw new RuntimeException(err.getMessage());
        
		} finally {
			destroy();
			
			if (LOG.isInfoEnabled()) {
				LOG.info("DaapServer stoped");
			}
        }
	}
	
	public void update() {
		if (connections != null) {
			synchronized(connections) {
				Iterator it = connections.iterator();
				while(it.hasNext()) {
					
					DaapConnection conn = (DaapConnection)it.next();
					
					if (!conn.isAudioStream()) {
						try {
							conn.update();
						} catch (IOException err) {
							LOG.error(err);
						}
					}
				}
			}
		}
	}
	
	void destroy() {
		
		if (serverSocket != null) {
			try {
				serverSocket.close();
				serverSocket = null;
			} catch (IOException err) {
				LOG.error("Error while closing connection", err);
			}
		}
		
		if (connections != null && connections.size() != 0) {
			
			Iterator it = connections.iterator();
			while(it.hasNext()) {
				
				DaapConnection conn = (DaapConnection)it.next();
				conn.destroy();
			}
			
			connections.clear();
			connections = null;
		}
		
		if (sessionIds != null) {
			sessionIds.clear();
			sessionIds = null;
		}
		
		serve = false;
	}
	
	void processRequest(DaapConnection conn, DaapRequest request) throws IOException {
		
		boolean complete = false;
		
		if (request.isSongRequest()) {
		
			if (isSessionIdValid(new Integer(request.getSessionId()))) {
				//conn.setAudioStream(true);
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
		synchronized(connections) {
			connections.remove(conn);
		}
		
		if (conn.isAudioStream()==false) {
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

	public boolean isRunning() {
        if(acceptThread == null) {
            return false;
        }
        return acceptThread.isAlive();
    }
	
	public void start() {
		if (!serve) {
		
			serve = true;
			acceptThread = new Thread(group, this, "DaapServer");
			acceptThread.setDaemon(true);
			acceptThread.start();
			
		} else if (LOG.isInfoEnabled()) {
			LOG.info("Server is already running");
		}
	}
	
	public void stop() {
		
		if (serve) {
			acceptThread = null;
		}
		
		serve = false;
	}
}
