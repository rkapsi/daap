/* 
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004 Roger Kapsi, info at kapsi dot de
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
 * DaapRequestProcessor processes a DaapRequest and generates
 * the appropriate DaapResponse.
 *
 * @author  Roger Kapsi
 */
public class DaapRequestProcessor {
    
    private static final Log LOG = LogFactory.getLog(DaapRequestProcessor.class);
    
    private static final String ISO_8859_1 = "8859_1";
    
    private DaapResponseFactory factory;
    
    /** Creates a new instance of DaapRequestProcessor */
    public DaapRequestProcessor(DaapResponseFactory factory) {
        this.factory = factory;
    }
    
    /**
     * Processes the request and returns the appropiate
     * DaapResponse (note: can be null which is valid and
     * means basically <tt>do nothing</tt>). Invalid requests
     * and all other errors throw an IOException which should
     * result in an immediate disconnect!
     *
     * @param request
     * @throws IOException
     * @return
     */    
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

                return factory.createAuthResponse(request);
            }

            if (request.isContentCodesRequest()) {
                return processContentCodesRequest(request);

            } else if (request.isLoginRequest()) {
                return processLoginRequest(request);

            // The following requests require a vaild 
            // session id
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
     * or if no authentication is required (if disabled). 
     */
    private boolean isAuthenticated(DaapRequest request)
            throws UnsupportedEncodingException {
        
        boolean authenticated = request.isServerSideRequest();
        
        if ( ! authenticated ) {
            
            DaapConnection connection = request.getConnection();
            DaapServer server = connection.getServer();
            DaapAuthenticator authenticator = server.getAuthenticator();
            
            authenticated = authenticator == null ||
                    !authenticator.requiresAuthentication();
            
            if ( ! authenticated ) {
                
                Header authHeader = request.getHeader("Authorization");
                
                if (authHeader != null) {
                    
                    StringTokenizer tok =
                    new StringTokenizer(authHeader.getValue(), " ");
                    
                    if (tok.nextToken().equals("Basic") == false) {
                        return false;
                    }
                    
                    byte[] logpass = Base64.decode(tok.nextToken().getBytes(ISO_8859_1));
                    
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
    
    /**
     * Checks if the SessionId of the request is valid
     */
    private boolean validateSessionId(DaapRequest request) {
        
        DaapConnection connection = request.getConnection();
        DaapSession session = connection.getSession(false);
        
        if (session != null) {
            
            return session.getSessionId().intValue() == request.getSessionId();
        }
        
        return false;
    }
    
    /**
     *
     * @param request
     * @throws IOException
     * @return
     */    
    protected DaapResponse processServerInfoRequest(DaapRequest request) 
            throws IOException {
        
        DaapConnection connection = request.getConnection();
        DaapServer server = connection.getServer();
        byte[] data = (byte[])server.getLibrary().select(request);
        
        if (data == null) {
            // request was either illegal or the protocol version
            // is not supported
            throw new IOException("library.select(ServerInfoRequest) returned null");
        }
        
        return factory.createChunkResponse(request, data);
    }
    
    /**
     *
     * @param request
     * @throws IOException
     * @return
     */    
    protected DaapResponse processContentCodesRequest(DaapRequest request)
            throws IOException {
        
        DaapConnection connection = request.getConnection();
        DaapServer server = connection.getServer();
        byte[] data = (byte[])server.getLibrary().select(request);
        
        if (data == null) {
            // in theory not possible
            throw new IOException("library.select(ContentCodesRequest) returned null");
        }
        
        return factory.createChunkResponse(request, data);
    }
    
    /**
     *
     * @param request
     * @throws IOException
     * @return
     */    
    protected DaapResponse processLoginRequest(DaapRequest request)
            throws IOException {
        
        DaapConnection connection = request.getConnection();
        Integer sessionId = connection.getSession(true).getSessionId();
        LoginResponseImpl login = new LoginResponseImpl(sessionId.intValue());
        
        byte[] data = DaapUtil.serialize(login, true);
        return factory.createChunkResponse(request, data);
    }
    
    /**
     *
     * @param request
     * @throws IOException
     * @return
     */    
    protected DaapResponse processLogoutRequest(DaapRequest request)
            throws IOException {
        
        throw new IOException("Logout");
    }
    
    /**
     *
     * @return
     * @param request
     * @throws IOException
     */    
    protected DaapResponse processUpdateRequest(DaapRequest request)
            throws IOException {
        
        DaapConnection connection = request.getConnection();
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
        
        return factory.createChunkResponse(request, data);
    }
    
    /**
     *
     * @param request
     * @throws IOException
     * @return
     */    
    protected DaapResponse processDatabasesRequest(DaapRequest request)
            throws IOException {
        
        DaapConnection connection = request.getConnection();
        DaapServer server = connection.getServer();
        byte[] serverDatabases = (byte[])server.getLibrary().select(request);
        
        if (serverDatabases == null) {
            // request was either illegal or the requested revision
            // is no longer available (server updateded to fast and
            // this client couldn't keep up)
            throw new IOException("library.select(DatabasesRequest) returned null");
        }
        
        return factory.createChunkResponse(request, serverDatabases);
    }
    
    /**
     *
     * @param request
     * @throws IOException
     * @return
     */    
    protected DaapResponse processDatabaseSongsRequest(DaapRequest request)
            throws IOException {
        
        DaapConnection connection = request.getConnection();
        DaapServer server = connection.getServer();
        byte[] databaseSongs = (byte[])server.getLibrary().select(request);
        
        if (databaseSongs == null) {
            // see processDatabasesRequest()
            throw new IOException("library.select(DatabaseSongsRequest) returned null");
        }
        
        return factory.createChunkResponse(request, databaseSongs);
    }
    
    /**
     *
     * @param request
     * @throws IOException
     * @return
     */    
    protected DaapResponse processDatabasePlaylistsRequest(DaapRequest request)
            throws IOException {
        
        DaapConnection connection = request.getConnection();
        DaapServer server = connection.getServer();
        byte[] databasePlaylists = (byte[])server.getLibrary().select(request);
        
        if (databasePlaylists == null) {
            // see processDatabasesRequest()
            throw new IOException("library.select(DatabasePlaylists) returned null");
        }
        
        return factory.createChunkResponse(request, databasePlaylists);
    }
    
    /**
     *
     * @param request
     * @throws IOException
     * @return
     */    
    protected DaapResponse processPlaylistSongsRequest(DaapRequest request)
            throws IOException {
        
        DaapConnection connection = request.getConnection();
        DaapServer server = connection.getServer();
        byte[] playlistSongs = (byte[])server.getLibrary().select(request);
        
        if (playlistSongs == null) {
            // see processDatabasesRequest()
            throw new IOException("library.select(PlaylistSongs) returned null");
        }
        
        return factory.createChunkResponse(request, playlistSongs);
    }
    
    /**
     *
     * @param request
     * @throws IOException
     * @return
     */    
    protected DaapResponse processResolveRequest(DaapRequest request)
            throws IOException {
       
        throw new IOException("Resolve is not implemented");
    }
    
    
    /**
     *
     * @param request
     * @throws IOException
     * @return
     */    
    protected DaapResponse processSongRequest(DaapRequest request)
            throws IOException {
        
        DaapConnection connection = request.getConnection();
        DaapServer server = connection.getServer();      
        DaapStreamSource streamSource = server.getStreamSource();
         
        if (streamSource != null) {
         
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
         
            return factory.createAudioResponse(request, song, in, pos, end);
        }
        
        return null;
    }
    
    
    
    /*
     * Returns the range which should be streamed.
     *
     * @param request
     * @throws IOException
     * @return
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
                        LOG.info("Unknown range type: " + key);
                    return null;
                }
                
                byte[] range = tok.nextToken().getBytes(ISO_8859_1);
                
                int q = 0;
                for(;q<range.length && range[q] != '-';q++);
                
                int pos = -1;
                int end = -1;
                
                    
                pos = Integer.parseInt(new String(range,0,q));

                q++;
                if (range.length-q != 0) {
                    end = Integer.parseInt(new String(range,q,range.length-q));
                }

                return (new int[]{pos, end});
                
            } catch (NoSuchElementException err) {
                // not critical, we can recover...
                LOG.error(err);
                
            } catch (NumberFormatException err) {
                // not critical, we can recover...
                LOG.error(err);
            }
        }
        
        // play from begin to end
        return (new int[]{0,-1});
    }
}
