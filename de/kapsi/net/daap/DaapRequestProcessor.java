/*
 * DaapRequestProcessor.java
 *
 * Created on April 5, 2004, 6:31 PM
 */

package de.kapsi.net.daap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpParser;
import org.apache.commons.httpclient.util.Base64;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.Song;
import de.kapsi.net.daap.DaapServer;
import de.kapsi.net.daap.DaapRequest;
import de.kapsi.net.daap.DaapSession;
import de.kapsi.net.daap.DaapAuthenticator;
import de.kapsi.net.daap.DaapStreamSource;

import de.kapsi.net.daap.chunks.UpdateResponseImpl;
import de.kapsi.net.daap.chunks.LoginResponseImpl;

import de.kapsi.net.daap.chunks.impl.ServerDatabases;
import de.kapsi.net.daap.chunks.impl.PlaylistSongs;
import de.kapsi.net.daap.chunks.impl.DatabaseSongs;
import de.kapsi.net.daap.chunks.impl.DatabasePlaylists;

/**
 *
 * @author  roger
 */
public class DaapRequestProcessor {
    
    private static final Log LOG = LogFactory.getLog(DaapRequestProcessor.class);
    
    private DaapConnection connection;
    private DaapResponseFactory factory;
    
    /** Creates a new instance of DaapRequestProcessor */
    public DaapRequestProcessor(DaapConnection connection, DaapResponseFactory factory) {
        this.connection = connection;
        this.factory = factory;
    }
    
    public DaapResponse process(DaapRequest request) throws IOException {
        
        if (request == null || request.isUnknownRequest()) {
            
            throw new IOException("Unknown request: " + request);
        }
        
        if (request.isSongRequest()) {
            return processSongRequest(request);

        } else if (request.isServerInfoRequest()) {
            return processServerInfoRequest(request);

        } else if (request.isLogoutRequest()) {
            return processLogoutRequest(request);

        } else {

            if ( ! isAuthenticated(request)) {

                //return new DaapAuthResponse(connection);
                return factory.createAuthResponse();
            }

            if (request.isContentCodesRequest()) {
                return processServerInfoRequest(request);

            } else if (request.isLoginRequest()) {
                return processLoginRequest(request);

            } else if (validateSessionId(request)) {

                if (request.isUpdateRequest()) {
                    return processUpdateRequest(request);

                } else if (request.isDatabasesRequest()) {
                    return processDatabasesRequest(request);

                } else if (request.isDatabaseSongsRequest()) {
                    return processDatabaseSongsRequest(request);

                } else if (request.isDatabasePlaylistsRequest()) {
                    return processDatabasePlaylistsRequest(request);

                } else if (request.isPlaylistSongsRequest()) {
                    return processPlaylistSongsRequest(request);

                } else if (request.isResolveRequest()) {
                    return processResolveRequest(request);

                }
            }
        }
        
        throw new IOException("Unhandled request: " + request);
    }
    
    /**
     * Returns <tt>true</tt> if request is authenticated
     */
    private boolean isAuthenticated(DaapRequest request)
            throws UnsupportedEncodingException {
        
        boolean authenticated = request.isServerSideRequest();
        
        if ( ! authenticated ) {
            
            DaapServer server = connection.getServer();
            DaapAuthenticator authenticator = server.getAuthenticator();
            
            authenticated = authenticator != null &&
            !authenticator.requiresAuthentication();
            
            if ( ! authenticated ) {
                
                Header authHeader = request.getHeader("Authorization");
                
                if (authHeader != null) {
                    
                    StringTokenizer tok =
                    new StringTokenizer(authHeader.getValue(), " ");
                    
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
                }
            }
        }
        
        return authenticated;
    }
    
    private boolean validateSessionId(DaapRequest request) {
        
        DaapSession session = connection.getSession(false);
        
        if (session != null) {
            return session.getSessionId().intValue() == request.getSessionId();
        }
        
        return false;
    }
    
    protected DaapResponse processServerInfoRequest(DaapRequest request) 
            throws IOException {
        
        DaapServer server = connection.getServer();
        byte[] data = DaapUtil.serialize(server.getServerInfoResponse(), true);
        return factory.createChunkResponse(data);
    }
    
    protected DaapResponse processContentCodesRequest(DaapRequest request)
            throws IOException {
                
        DaapServer server = connection.getServer();
        byte[] data = DaapUtil.serialize(server.getContentCodesResponse(), true);
        return factory.createChunkResponse(data);
    }
    
    protected DaapResponse processLoginRequest(DaapRequest request)
            throws IOException {
        
        Integer sessionId = connection.getSession(true).getSessionId();
        LoginResponseImpl login = new LoginResponseImpl(sessionId.intValue());
        
        byte[] data = DaapUtil.serialize(login, true);
        return factory.createChunkResponse(data);
    }
    
    protected DaapResponse processLogoutRequest(DaapRequest request)
            throws IOException {
        
        throw new IOException("Logout");
    }
    
    protected DaapResponse processUpdateRequest(DaapRequest request)
            throws IOException {
        
        DaapServer server = connection.getServer();
        Integer revision = (Integer)server.getLibrary().select(request);
        
        if (revision == null) {
            // should never happen but who knows :p
            throw new IOException("library.select(UpdateRequest) returned null-Revision");
        }
        
        DaapSession session = connection.getSession(false);
            
        if (revision.intValue() == request.getDelta() && revision.intValue() != 0) {
            
            session.addAttribute("DELTA", new Integer(request.getDelta()));
            session.addAttribute("REVISION-NUMBER", new Integer(request.getRevisionNumber()));

            return null;
        }
        
        // if revision is 0 (i.e. no database available) will iTunes
        // disconnect...
        
        UpdateResponseImpl update = new UpdateResponseImpl(revision.intValue());
        byte[] data = DaapUtil.serialize(update, true);
        
        return factory.createChunkResponse(data);
    }
    
    protected DaapResponse processDatabasesRequest(DaapRequest request)
            throws IOException {
        
        DaapServer server = connection.getServer();
        byte[] serverDatabases
            = (byte[])server.getLibrary().select(request);
        
        if (serverDatabases == null) {
            // request was either illegal or the requested revision
            // is no longer available (server updateded to fast and
            // this client couldn't keep up)
            throw new IOException("library.select(DatabasesRequest) returned null");
        }
        
        return factory.createChunkResponse(serverDatabases);
    }
    
    protected DaapResponse processDatabaseSongsRequest(DaapRequest request)
            throws IOException {
        
        DaapServer server = connection.getServer();
        byte[] databaseSongs
            = (byte[])server.getLibrary().select(request);
        
        if (databaseSongs == null) {
            // see processDatabasesRequest()
            throw new IOException("library.select(DatabaseSongsRequest) returned null");
        }
        
        //return new DaapChunkResponse(connection, databaseSongs);
        return factory.createChunkResponse(databaseSongs);
    }
    
    protected DaapResponse processDatabasePlaylistsRequest(DaapRequest request)
            throws IOException {
        
        DaapServer server = connection.getServer();
        byte[] databasePlaylists
            = (byte[])server.getLibrary().select(request);
        
        if (databasePlaylists == null) {
            // see processDatabasesRequest()
            throw new IOException("library.select(DatabasePlaylists) returned null");
        }
        
        //return new DaapChunkResponse(connection, databasePlaylists);
        return factory.createChunkResponse(databasePlaylists);
    }
    
    protected DaapResponse processPlaylistSongsRequest(DaapRequest request)
            throws IOException {
        
        DaapServer server = connection.getServer();
        byte[] playlistSongs
                = (byte[])server.getLibrary().select(request);
        
        if (playlistSongs == null) {
            // see processDatabasesRequest()
            throw new IOException("library.select(PlaylistSongs) returned null");
        }
        
        //return new DaapChunkResponse(connection, playlistSongs);
        
        return factory.createChunkResponse(playlistSongs);
    }
    
    protected DaapResponse processResolveRequest(DaapRequest request)
            throws IOException {
       
        throw new IOException("Resolve is not implemented");
    }
    
    // BEGIN AUDIO
    
    /**
     *
     */
    protected DaapResponse processSongRequest(DaapRequest request)
            throws IOException {
        
        DaapServer server = connection.getServer();      
        DaapStreamSource streamSource = server.getStreamSource();
         
        if (streamSource != null && request.isSongRequest()) {
         
            int[] range = getRange(request);
         
            if (range == null) {
                throw new IOException("getRange returned null");
            }
         
            int pos = range[0];
            int end = range[1];
         
            Song song = (Song)server.getLibrary().select(request);
         
            if (song == null) {
                throw new IOException("Library returned null-Song for request: " + request);
            }
         
            if (end == -1) {
                end = song.getSize();
            }
         
            FileInputStream in = streamSource.getSource(song);
         
            if (in == null) {
                throw new IOException("Unknown source for Song: " + song);
            }
         
            return factory.createAudioResponse(in, pos, end);
        }
        
        return null;
    }
    
    /**
     * Returns a range from where to where shall be played
     */
    private int[] getRange(DaapRequest request)
            throws IOException {
        
        Header rangeHeader = request.getHeader("Range");
        
        if (rangeHeader != null) {
            try {
                StringTokenizer tok = new StringTokenizer(rangeHeader.getValue(), "=");
                String key = tok.nextToken().trim();
                
                if (key.equals("bytes")==false) {
                    if (LOG.isInfoEnabled())
                        LOG.info("unknown type: " + key);
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
        
        return (new int[]{0,-1});
    }
}
