
package de.kapsi.net.daap;

import java.io.*;
import java.util.*;
import java.net.SocketException;
import de.kapsi.net.daap.chunks.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handles all request except streaming
 */
public class DaapRequestHandler {
	
	private static final Log LOG = LogFactory.getLog(DaapRequestHandler.class);
	
	private Library library;
	private ServerInfoResponse serverInfo;
	private ContentCodesResponse contentCodes;
	
    private DaapStreamSource streamSource;
	private DaapAuthenticator authenticator;
	
	public DaapRequestHandler(ServerInfoResponse serverInfo, 
                ContentCodesResponse contentCodes, Library library) {
                
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
	
    public void setStreamSource(DaapStreamSource streamSource) {
		this.streamSource = streamSource;
	}
	
	public DaapStreamSource getStreamSource() {
		return streamSource;
	}
    
	public boolean processRequest(DaapConnection conn, DaapRequest request) 
            throws IOException {
		
		if (request == null || request.isUnknownRequest()) {
		
			if (LOG.isWarnEnabled()) {
				LOG.warn("Unknown request: " + request);
			}
			
			// disconnect...
			return false;
		}
		
        if (request.isSongRequest()) {
            processSongRequest(conn, request);
            return false;
            
        } else if (request.isServerInfoRequest()) {
			return processServerInfoRequest(conn, request);
		
		} else if (request.isLogoutRequest()) {
			return processLogoutRequest(conn, request);
		
		} else {
			
            if ( ! isAuthenticated(request)) {
                DaapResponse response = 
                    DaapResponse.createAuthResponse(request);
            
                return response.processRequest(conn);
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
				
				}
			}
		}
		
		return false;
	}
	
    /**
     * Returns <tt>true</tt> if request is authenticated
     */
    private boolean isAuthenticated(DaapRequest request) 
            throws UnsupportedEncodingException {
        
        boolean authenticated = request.isServerSideRequest();
        
        if ( ! authenticated ) {
        
            authenticated = authenticator != null && 
                                !authenticator.requiresAuthentication();
                                
            if ( ! authenticated ) {
            
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
                        
                        if (!authenticated && LOG.isInfoEnabled()) {
                            LOG.info("Wrong username or password");
                        }

                        break;
                    }
                }
            }
        }
        
        return authenticated;
    }
    
	private boolean processServerInfoRequest(DaapConnection conn, DaapRequest request) 
		throws IOException {
		
		DaapResponse response = 
			DaapResponse.createResponse(request, serverInfo);
			
		return response.processRequest(conn);
	}
	
	private boolean processContentCodesRequest(DaapConnection conn, DaapRequest request)
		throws IOException {
		
		DaapResponse response = 
			DaapResponse.createResponse(request, contentCodes);
			
		return response.processRequest(conn);
	}
	
	private boolean processLoginRequest(DaapConnection conn, DaapRequest request)
		throws IOException {
		
		DaapSession session = conn.getSession(true);
		
		LoginResponseImpl login = new LoginResponseImpl(session.getSessionId().intValue());
		
		DaapResponse response = 
			DaapResponse.createResponse(request, login);
			
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
				DaapResponse.createResponse(request, update);
				
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
			DaapResponse.createResponse(request, serverDatabases);
			
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
			DaapResponse.createResponse(request, databaseSongs);
			
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
			DaapResponse.createResponse(request, databasePlaylists);
			
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
			DaapResponse.createResponse(request, playlistSongs);
			
		return response.processRequest(conn);
	}
	
	private boolean processResolveRequest(DaapConnection conn, DaapRequest request) 
		throws IOException {
		
		LOG.warn("IMPLEMENT processResolveRequest()! NOTE: /resolve is never issued by iTunes/4.2!");
		
		return false;
	}
    
    // BEGIN AUDIO
    
    /**
     *
     */
    private boolean processSongRequest(DaapConnection conn, DaapRequest request) 
		throws IOException {
		
		if (streamSource != null && request.isSongRequest()) {
		
			int[] range = getRange(request);
			
			if (range == null) {
				if (LOG.isInfoEnabled())
					LOG.info("getRange returned null");
				return false;
			}
			
			int begin = range[0];
			int end = range[1];
			
			int length = 0;
			
			Song song = (Song)library.select(request);
			
			if (song == null) {
				if (LOG.isInfoEnabled())
					LOG.info("Library returned null-Song for request: " + request);
				return false;
			}
			
			if (end == -1) {
				length = song.getSize()-begin;
			} else {
				length = end - begin;
			}
			
			DaapResponse response = DaapResponse.createAudioResponse(request, length);
			response.processAudioRequest(conn);
			
            OutputStream out = conn.getOutputStream();
            InputStream in = streamSource.getSource(song);
            
            if (in == null) {
                if (LOG.isInfoEnabled())
                    LOG.info("Unknown source for Song: " + song);
                return false;
            }
            
            try {
                stream(in, out, begin, length);
            } finally {
                // make sure we close the InputStream
                in.close();
            }
		}
		
		return false;
	}
	
    /**
     * Returns a range from where to where shall be played
     */
	private int[] getRange(DaapRequest request) 
            throws IOException {
		
		Header[] headers = request.getHeaders();
		for(int i = 0; i < headers.length; i++) {
			Header header = headers[i];
			if (header.getName().equals("Range")) {
				try {
					StringTokenizer tok = new StringTokenizer(header.getValue(), "=");
					String key = tok.nextToken();
					
					if (key.equals("bytes")==false) { 
                        if (LOG.isInfoEnabled())
                            LOG.info("unknown type");
						return null; 
					}
					
					byte[] range = tok.nextToken().getBytes("UTF-8");
					
					int q = 0;
					for(;q<range.length && range[q] != '-';q++);
					
					int begin = Integer.parseInt(new String(range,0,q));
					
					q++;
					int end = -1;
					
					if (range.length-q != 0) {
						end = Integer.parseInt(new String(range,q,range.length-q));
					}
					
					return (new int[]{begin, end});
					
				} catch (NoSuchElementException err) {
					LOG.error(err);
				} catch (NumberFormatException err) {
					LOG.error(err);
				}
			}
		}
		
		return (new int[]{0,-1});
	}
    
    /**
     * Reads from <tt>in</tt> and writes to <tt>out</tt>
     */
    private void stream(InputStream in, OutputStream out, int begin, int length) 
            throws IOException {
            
        try {
            
            // DO NOT THIS TO SET THIS TOO HIGH AS IT 
            // CAUSES RE-BUFFERING AT THE BEGINNING 
            // OF HIGH BIT RATE SONGS (WAV AND AIFF) !!! 
            byte[] buffer = new byte[512];
            
            int total = 0;
            int len = -1;
            
            if (begin != 0) {
                in.skip(begin);
            }
            
            while((len = in.read(buffer, 0, buffer.length)) != -1 && total < length) {
                out.write(buffer, 0, len);
                
                // DO NOT FLUSH AS IT CAUSES RE-BUFFERING AT THE 
                // BEGINNING OF HIGH BIT RATE SONGS (WAV AND AIFF) !!!
                
                total += len;
            }
            
            out.flush();
            in.close();
            
        } finally {
            in.close();
        }
    }
}
