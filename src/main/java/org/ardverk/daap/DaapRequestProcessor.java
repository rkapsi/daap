/*
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004-2010 Roger Kapsi
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

package org.ardverk.daap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.ardverk.daap.chunks.Chunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DaapRequestProcessor processes a DaapRequest and generates the appropriate
 * DaapResponse.
 * 
 * @author Roger Kapsi
 */
public class DaapRequestProcessor {

    private static final Logger LOG = LoggerFactory
            .getLogger(DaapRequestProcessor.class);

    private DaapResponseFactory factory;

    /** Creates a new instance of DaapRequestProcessor */
    public DaapRequestProcessor(DaapResponseFactory factory) {
        this.factory = factory;
    }

    /**
     * Processes the request and returns the appropiate DaapResponse (note: can
     * be null which is valid and means basically <tt>do nothing</tt>). Invalid
     * requests and all other errors throw an IOException which should result in
     * an immediate disconnect!
     * 
     * @param request
     * @throws IOException
     * @return a DaapResponse for the request
     */
    public DaapResponse process(DaapRequest request) throws IOException {

        if (request == null || request.isUnknownRequest()) {
            throw new IOException("Unknown request: " + request);
        }

        // Unlock code in processUpdateRequest()
        request.getConnection().lock();

        if (request.isSongRequest()) {
            return processSongRequest(request);

        } else if (request.isServerInfoRequest()) {
            return processServerInfoRequest(request);

        } else if (request.isLogoutRequest()) {
            return processLogoutRequest(request);

        } else {

            if (!isAuthenticated(request)) {
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

            } else {
                throw new IOException("Invalid session-id: " + request);
            }
        }

        throw new IOException("Unhandled request: " + request);
    }

    /**
     * Returns <tt>true</tt> if request is authenticated or if no authentication
     * is required (if disabled).
     */
    private boolean isAuthenticated(DaapRequest request)
            throws UnsupportedEncodingException {

        if (request.isServerSideRequest()) {
            return true;
        }

        DaapConnection connection = request.getConnection();
        DaapServer<?> server = request.getServer();
        DaapConfig config = server.getConfig();
        DaapAuthenticator authenticator = server.getAuthenticator();

        if (authenticator == null) {
            return true;
        }

        if (config.getAuthenticationMethod().equals(DaapConfig.NO_PASSWORD)) {
            return true;
        }

        Header authHeader = request.getHeader(DaapRequest.AUTHORIZATION);
        if (authHeader == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info(DaapRequest.AUTHORIZATION + " header is not set");
            }
            return false;
        }

        String authValue = authHeader.getValue();

        Object scheme = config.getAuthenticationScheme();
        if (scheme.equals(DaapConfig.BASIC_SCHEME)) {

            StringTokenizer tok = new StringTokenizer(authHeader.getValue(),
                    " ");

            if (tok.nextToken().equals("Basic") == false) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Schemes mismatch");
                }
                return false;
            }

            byte[] logpass = Base64.decodeBase64(tok.nextToken().getBytes(
                    DaapUtil.ISO_8859_1));

            int q = 0;
            for (; q < logpass.length && logpass[q] != ':'; q++)
                ;

            String username = new String(logpass, 0, q, DaapUtil.UTF_8);

            q++;
            String password = new String(logpass, q, logpass.length - q,
                    DaapUtil.UTF_8);

            // Success!
            return authenticator.authenticate(username, password, null, null);

        } else {
            if (!authValue.startsWith("Digest")) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Schemes mismatch");
                }
                return false;
            }

            int beginIndex = "Digest".length() + 1;
            if (beginIndex >= authValue.length()) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Illegal Authorization Header");
                }
                return false;
            }

            // TODO: better/safer splitting
            String[] values = authValue.substring(beginIndex).split(", ");

            String username = null;
            String nonce = null;
            String uri = null;
            String response = null;

            for (int i = 0; i < values.length; i++) {
                String[] kv = values[i].split("=", 2);
                if (kv.length != 2) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Illegal Authorization Header: " + values[i]);
                    }
                    return false;
                }

                if (!kv[1].startsWith("\"") || !kv[1].endsWith("\"")) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Illegal Authorization Header: " + values[i]);
                    }
                    return false;
                }

                String key = kv[0].trim().toLowerCase(Locale.US);
                String value = kv[1].substring(1, kv[1].length() - 1);

                if (key.equals("username")) {
                    username = value;
                } else if (key.equals("nonce")) {
                    nonce = value;
                } else if (key.equals("uri")) {
                    uri = value;
                } else if (key.equals("response")) {
                    response = value;
                }

                if (username != null && nonce != null && uri != null
                        && response != null) {
                    break;
                }
            }

            if (username == null) {
                LOG.info("Username is null");
                return false;
            }

            if (nonce == null) {
                LOG.info("Nonce is null");
                return false;
            }

            if (uri == null) {
                LOG.info("URI is null");
                return false;
            }

            if (response == null) {
                LOG.info("Response is null");
                return false;
            }

            String currentNonce = connection.getNonce();
            if (currentNonce == null || !currentNonce.equals(nonce)) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Nonce mismatch: " + currentNonce + " vs. "
                            + nonce);
                }
                return false;
            }

            /*
             * byte[] password = authenticator.getPassword(username,
             * DaapConfig.DIGEST_SCHEME); if (password == null) { if
             * (LOG.isInfoEnabled()) { LOG.info("Password is null"); } return
             * false; }
             * 
             * String ha1 = DaapUtil.toHexString(password); String ha2 =
             * DaapUtil.calculateHA2(uri);
             * 
             * String digest = DaapUtil.digest(ha1, ha2, nonce);
             * 
             * // Success? return digest.equalsIgnoreCase(response);
             */

            return authenticator.authenticate(username, response, uri, nonce);
        }
    }

    /**
     * Checks if the SessionId of the request is valid
     */
    private boolean validateSessionId(DaapRequest request) {

        DaapConnection connection = request.getConnection();
        DaapSession session = connection.getSession(false);

        if (session != null) {
            return session.getSessionId().equals(request.getSessionId());
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

        Library library = request.getHeadLibrary();
        if (library == null) {
            throw new IOException("Connection is not associated with a Library");
        }

        Chunk chunk = (Chunk) library.select(request);
        if (chunk == null) {
            // request was either illegal or the protocol version
            // is not supported
            throw new IOException(
                    "library.select(ServerInfoRequest) returned null");
        }

        byte[] data = DaapUtil.serialize(chunk, request.isGZIPSupported());
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

        Library library = request.getHeadLibrary();
        if (library == null) {
            throw new IOException("Connection is not associated with a Library");
        }

        Chunk chunk = (Chunk) library.select(request);
        if (chunk == null) {
            // in theory not possible
            throw new IOException(
                    "library.select(ContentCodesRequest) returned null");
        }

        byte[] data = DaapUtil.serialize(chunk, request.isGZIPSupported());
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

        if (!request.getSessionId().equals(SessionId.INVALID)) {
            throw new IOException("Session ID cannot exist: "
                    + request.getSessionId());
        }

        Library library = request.getHeadLibrary();
        if (library == null) {
            throw new IOException("Connection is not associated with a Library");
        }

        // Create session...
        DaapConnection connection = request.getConnection();
        DaapSession session = connection.getSession(true);
        request.setSessionId(session.getSessionId());

        Chunk chunk = (Chunk) library.select(request);
        if (chunk == null) {
            throw new IOException("library.select(LoginRequest) returned null");
        }

        byte[] data = DaapUtil.serialize(chunk, request.isGZIPSupported());
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

        DaapConnection connection = request.getConnection();
        DaapSession session = connection.getSession(false);

        if (request.isKeepConnectionAlive() && session != null) {
            return factory.createNoContentResponse(request);
        }

        // Do nothing, just throw a IOE which will disconnect the client
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

        Library library = request.nextLibrary();
        if (library == null) {
            throw new IOException("Connection is not associated with a Library");
        }

        if (library.getRevision() == request.getDelta()) {
            request.getConnection().unlock();
            DaapSession session = request.getConnection().getSession(false);
            if (session == null) {
                throw new IOException(
                        "Connection is not associated with a Session");
            }
            session.setAttribute("CLIENT_REVISION", new Integer(library
                    .getRevision()));
            return null;
        }

        Chunk chunk = (Chunk) library.select(request);
        if (chunk == null) {
            throw new IOException("library.select(UpdateRequest) returned null");
        }

        byte[] data = DaapUtil.serialize(chunk, request.isGZIPSupported());
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

        Library library = request.getHeadLibrary();
        if (library == null) {
            throw new IOException("Connection is not associated with a Library");
        }

        Chunk serverDatabases = (Chunk) library.select(request);
        if (serverDatabases == null) {
            // request was either illegal or the requested revision
            // is no longer available (server updateded to fast and
            // this client couldn't keep up)
            throw new IOException(
                    "library.select(DatabasesRequest) returned null");
        }

        byte[] data = DaapUtil.serialize(serverDatabases, request
                .isGZIPSupported());
        return factory.createChunkResponse(request, data);
    }

    /**
     * 
     * @param request
     * @throws IOException
     * @return
     */
    protected DaapResponse processDatabaseSongsRequest(DaapRequest request)
            throws IOException {

        Library library = request.getHeadLibrary();
        if (library == null) {
            throw new IOException("Connection is not associated with a Library");
        }

        Chunk databaseSongs = (Chunk) library.select(request);
        if (databaseSongs == null) {
            // see processDatabasesRequest()
            throw new IOException(
                    "library.select(DatabaseSongsRequest) returned null");
        }

        byte[] data = DaapUtil.serialize(databaseSongs, request
                .isGZIPSupported());
        return factory.createChunkResponse(request, data);
    }

    /**
     * 
     * @param request
     * @throws IOException
     * @return
     */
    protected DaapResponse processDatabasePlaylistsRequest(DaapRequest request)
            throws IOException {

        Library library = request.getHeadLibrary();
        if (library == null) {
            throw new IOException("Connection is not associated with a Library");
        }

        Chunk databasePlaylists = (Chunk) library.select(request);
        if (databasePlaylists == null) {
            // see processDatabasesRequest()
            throw new IOException(
                    "library.select(DatabasePlaylists) returned null");
        }

        byte[] data = DaapUtil.serialize(databasePlaylists, request
                .isGZIPSupported());
        return factory.createChunkResponse(request, data);
    }

    /**
     * 
     * @param request
     * @throws IOException
     * @return
     */
    protected DaapResponse processPlaylistSongsRequest(DaapRequest request)
            throws IOException {

        Library library = request.getHeadLibrary();
        if (library == null) {
            throw new IOException("Connection is not associated with a Library");
        }

        Chunk playlistSongs = (Chunk) library.select(request);
        if (playlistSongs == null) {
            // see processDatabasesRequest()
            throw new IOException("library.select(PlaylistSongs) returned null");
        }

        byte[] data = DaapUtil.serialize(playlistSongs, request
                .isGZIPSupported());
        return factory.createChunkResponse(request, data);
    }

    /**
     * Isn't implemented
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

        Library library = request.getLibrary();
        if (library == null) {
            throw new IOException("Connection is not associated with a Library");
        }

        DaapConnection connection = request.getConnection();
        DaapServer<?> server = connection.getServer();
        DaapStreamSource streamSource = server.getStreamSource();

        if (streamSource != null) {

            long[] range = getRange(request);

            if (range == null) {
                throw new IOException("getRange returned null");
            }

            long pos = range[0];
            long end = range[1];

            Song song = (Song) library.select(request);

            if (song == null) {
                throw new IOException(
                        "Library returned null-Song for request: " + request);
            }

            if (end == -1) {
                end = song.getSize();
            }

            Object src = streamSource.getSource(song);
            if (src instanceof File) {
                return factory.createAudioResponse(request, song, (File) src,
                        pos, end);
            } else if (src instanceof FileInputStream) {
                return factory.createAudioResponse(request, song,
                        (FileInputStream) src, pos, end);
            }/*
              * else if (src instanceof FileChannel) { return
              * factory.createAudioResponse(request, song, (FileChannel)src,
              * pos, end); }
              */else {
                throw new IOException("Unknown source [" + src + "] for Song: "
                        + song);
            }
        }

        return null;
    }

    /*
     * Returns the range which should be streamed.
     * 
     * @param request
     * 
     * @throws IOException
     * 
     * @return
     */
    private long[] getRange(DaapRequest request) throws IOException {

        Header rangeHeader = request.getHeader("Range");

        if (rangeHeader != null) {
            try {
                StringTokenizer tok = new StringTokenizer(rangeHeader
                        .getValue(), "=");
                String key = tok.nextToken().trim();

                if (key.equals("bytes") == false) {
                    if (LOG.isInfoEnabled())
                        LOG.info("Unknown range type: " + key);
                    return null;
                }

                byte[] range = tok.nextToken().getBytes(DaapUtil.ISO_8859_1);

                int q = 0;
                for (; q < range.length && range[q] != '-'; q++)
                    ;

                long pos = -1;
                long end = -1;

                pos = Long.parseLong(new String(range, 0, q));

                q++;
                if (range.length - q != 0) {
                    end = Long
                            .parseLong(new String(range, q, range.length - q));
                }

                return (new long[] { pos, end });

            } catch (NoSuchElementException err) {
                // not critical, we can recover...
                LOG.error("NoSuchElementException", err);

            } catch (NumberFormatException err) {
                // not critical, we can recover...
                LOG.error("NumberFormatException", err);
            }
        }

        // play from begin to end
        return (new long[] { 0, -1 });
    }
}