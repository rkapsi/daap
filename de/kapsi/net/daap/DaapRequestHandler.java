
package de.kapsi.net.daap;

import java.io.*;
import java.util.*;

import de.kapsi.net.daap.chunks.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DaapRequestHandler {
	
	private static final Log LOG = LogFactory.getLog(DaapRequestHandler.class);
	
	private Library library;
	private ServerInfoResponse serverInfo;
	private ContentCodesResponse contentCodes;
	
	private DaapAuthenticator authenticator;
	
	public DaapRequestHandler(ServerInfoResponse serverInfo, ContentCodesResponse contentCodes, Library library) {
		this.serverInfo = serverInfo;
		this.contentCodes = contentCodes;
		this.library = library;
	}
	
	public void setAuthenticator(DaapAuthenticator authenticator) {
		this.authenticator = authenticator;
	}
	
	public DaapAuthenticator getAuthenticator() {
		return authenticator;
	}
	
	public boolean processRequest(DaapConnection conn, DaapRequest request) throws IOException {
		
		if (request == null || request.isUnknownRequest()) {
		
			if (LOG.isWarnEnabled()) {
				LOG.warn("Unknown request: " + request);
			}
			
			// disconnect...
			return false;
		}
		
		if (request.isServerInfoRequest()) {
			return processServerInfoRequest(conn, request);
		
		} else if (request.isLogoutRequest()) {
			return processLogoutRequest(conn, request);
		
		} else {
			
			if (request.isServerSideRequest()==false) {
				if (authenticator != null && authenticator.requiresAuthentication()) {
					
					boolean authenticated = false;
					
					Header[] headers = request.getHeaders();
					
					for(int i = 0; i < headers.length; i++) {
						Header header = headers[i];
						if (header.getName().equals("Authorization")) {
						
							StringTokenizer tok = 
								new StringTokenizer(header.getValue(), " ");
							
							if (tok.nextToken().equals("Basic") == false) {
								return false;
							}
							
							byte[] logpass = Base64.decode(tok.nextToken().getBytes("UTF-8"));
					
							int q = 0;
							for(;q<logpass.length && logpass[q] != ':';q++);
							
							String username = new String(logpass,0,q);
							
							q++;
							String password = "";
							
							if (logpass.length-q != 0) {
								password = new String(logpass,q,logpass.length-q);
							}
							
							authenticated = authenticator.authenticate(username, password);
							break;
						}
					}
					
					if (!authenticated) {
						DaapResponse response = 
							DaapResponse.createAuthResponse();
				
						return response.processRequest(conn);
					}
				}
			}
			
			if (request.isContentCodesRequest()) {
				return processServerInfoRequest(conn, request);
	
			} else if (request.isLoginRequest()) {
				return processLoginRequest(conn, request);

			} else if (validateSessionId(conn, request)) {
				
				if (request.isUpdateRequest()) {
					return processUpdateRequest(conn, request);
				
				} else if (request.isDatabasesRequest()) {
					return processDatabasesRequest(conn, request);
				
				} else if (request.isDatabaseSongsRequest()) {
					return processDatabaseSongsRequest(conn, request);
				
				} else if (request.isDatabasePlaylistsRequest()) {
					return processDatabasePlaylistsRequest(conn, request);
				
				} else if (request.isPlaylistSongsRequest()) {
					return processPlaylistSongsRequest(conn, request);
				
				} else if (request.isResolveRequest()) {
					return processResolveRequest(conn, request);
				
				} else if (request.isSongRequest()) {
					return processSongRequest(conn, request);
				}
			}
		}
		
		return false;
	}
	
	private boolean processServerInfoRequest(DaapConnection conn, DaapRequest request) 
		throws IOException {
		
		DaapResponse response = 
			DaapResponse.createResponse(serverInfo);
			
		return response.processRequest(conn);
	}
	
	private boolean processContentCodesRequest(DaapConnection conn, DaapRequest request)
		throws IOException {
		
		DaapResponse response = 
			DaapResponse.createResponse(contentCodes);
			
		return response.processRequest(conn);
	}
	
	private boolean processLoginRequest(DaapConnection conn, DaapRequest request)
		throws IOException {
		
		DaapSession session = conn.getSession(true);
		
		LoginResponseImpl login = new LoginResponseImpl(session.getSessionId().intValue());
		
		DaapResponse response = 
			DaapResponse.createResponse(login);
			
		return response.processRequest(conn);
	}
	
	private boolean processLogoutRequest(DaapConnection conn, DaapRequest request)
		throws IOException {
		
		return false;
	}
	
	private boolean validateSessionId(DaapConnection conn, DaapRequest request) {
		
		DaapSession session = conn.getSession(false);
		
		if (session != null && session.isValid()) {
			int sessionId1 = request.getSessionId();
			int sessionId2 = session.getSessionId().intValue();
			return (sessionId1 == sessionId2);
		
		}
		
		return false;
	}
	
	private boolean processUpdateRequest(DaapConnection conn, DaapRequest request)
		throws IOException {
		
		synchronized(conn) {
		
			Integer revision = (Integer)library.select(request);
			
			if (revision == null) {
				// should never happen but who knows :p
				LOG.debug("library.select(UpdateRequest) returned null-Revision");
				return false;
			}
			
			DaapSession session = conn.getSession(false);
			
			if (revision.intValue() == request.getDelta() && revision.intValue() != 0) {
				
				session.removeAttribute("UPDATE_LOCK");
				session.addAttribute("DELTA", new Integer(request.getDelta()));
				session.addAttribute("REVISION-NUMBER", new Integer(request.getRevisionNumber()));
				
				conn.connectionKeepAlive();
				return true;
			}
			
			if (revision.intValue() != 0) {
				session.addAttribute("UPDATE_LOCK", "LOCK");
			}
			
			// if revision is 0 (i.e. no database available) will iTunes
			// disconnect...
			
			UpdateResponseImpl update = new UpdateResponseImpl(revision.intValue());
			
			DaapResponse response = 
				DaapResponse.createResponse(update);
				
			return response.processRequest(conn);
		}
	}
	
	private boolean processDatabasesRequest(DaapConnection conn, DaapRequest request) 
		throws IOException {
		
		ServerDatabases serverDatabases = (ServerDatabases)library.select(request);
		
		if (serverDatabases == null) {
			// request was either illegal or the requested revision
			// is no longer available (server updateded to fast and
			// this client couldn't keep up)
			return false;
		}
		
		DaapResponse response = 
			DaapResponse.createResponse(serverDatabases);
			
		return response.processRequest(conn);
	}
	
	private boolean processDatabaseSongsRequest(DaapConnection conn, DaapRequest request)
		throws IOException {
		
		DatabaseSongs databaseSongs = (DatabaseSongs)library.select(request);
		
		if (databaseSongs == null) {
			// see processDatabasesRequest()
			return false;
		}
		
		DaapResponse response = 
			DaapResponse.createResponse(databaseSongs);
			
		return response.processRequest(conn);
	}
	
	private boolean processDatabasePlaylistsRequest(DaapConnection conn, DaapRequest request) 
		throws IOException {
		
		DatabasePlaylists databasePlaylists = (DatabasePlaylists)library.select(request);
		
		if (databasePlaylists == null) {
			// see processDatabasesRequest()
			return false;
		}
		
		DaapResponse response = 
			DaapResponse.createResponse(databasePlaylists);
			
		return response.processRequest(conn);
	}
	
	private boolean processPlaylistSongsRequest(DaapConnection conn, DaapRequest request) 
		throws IOException {
		
		PlaylistSongs playlistSongs = (PlaylistSongs)library.select(request);
		
		if (playlistSongs == null) {
			// see processDatabasesRequest()
			return false;
		}
		
		DaapResponse response = 
			DaapResponse.createResponse(playlistSongs);
			
		return response.processRequest(conn);
	}
	
	private boolean processSongRequest(DaapConnection conn, DaapRequest request)
		throws IOException {
		
		LOG.warn("DaapAudioRequestHandler is responsible for this Request!");
	
		return false;
	}
	
	private boolean processResolveRequest(DaapConnection conn, DaapRequest request) 
		throws IOException {
		
		LOG.warn("IMPLEMENT processResolveRequest()! NOTE: /resolve is never issued by iTunes/4.2!");
		
		return false;
	}
}
