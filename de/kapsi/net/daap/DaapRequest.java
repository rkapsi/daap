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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A DAAP request. The data of this class is submitted by the client 
 * (e.g. iTunes) and the data is used to create a appropriate 
 * DaapResponse.
 * 
 * @author  Roger Kapsi
 */
public class DaapRequest {
    
    public static final String AUTHORIZATION = "Authorization";
    public static final String CLIENT_DAAP_VERSION = "Client-DAAP-Version";
    public static final String USER_AGENT = "User-Agent";
    
    /** "/server-info" */
    public static final int SERVER_INFO = 1;

    /** "/content-codes" */
    public static final int CONTENT_CODES = 2;

    /** "/login" */
    public static final int LOGIN = 3;

    /** "/logout" */
    public static final int LOGOUT = 4;

    /** "/update" */
    public static final int UPDATE = 5;

    /** "/resolve" */
    public static final int RESOLVE = 6;

    /** "/databases" */
    public static final int DATABASES = 7;

    /** "/databases/databaseId/items" */
    public static final int DATABASE_SONGS = 8;

    /** "/databases/databaseId/containers" */
    public static final int DATABASE_PLAYLISTS = 9;

    /** "/databases/databaseId/containers/containerId/items" */
    public static final int PLAYLIST_SONGS = 10;

    /** "/databases/databaseId/items/itemId.format" */
    public static final int SONG = 11;
    
    private static final Log LOG = LogFactory.getLog(DaapRequest.class);
    
    private String method;
    private URI uri;
    private String protocol;
    
    private Map<String, String> queryMap;
    
    private SessionId sessionId = SessionId.INVALID;
    private int revisionNumber = DaapUtil.NULL;
    private int delta = DaapUtil.NULL;
    
    private List<String> meta;
    private String metaString;
    
    private int requestType = DaapUtil.NULL;
    private long databaseId = DaapUtil.NULL;
    private long containerId = DaapUtil.NULL;
    private long itemId = DaapUtil.NULL;
    
    private List<Header> headers;
    private boolean isServerSideRequest;
    private boolean isUpdateType;
   
    private DaapConnection connection;
    
    /**
     * Create a new DaapRequest
     */
    private DaapRequest(DaapConnection connection) {
        this.connection = connection;
        headers = new ArrayList<Header>();
    }
    
    /**
     * Creates a server side fake update DaapRequest to issue an update
     *
     * @param sessionId
     * @param revisionNumber
     * @param delta
     */
    public DaapRequest(DaapConnection connection, SessionId sessionId, 
            int revisionNumber, int delta) {
        this(connection);
        
        this.sessionId = sessionId;
        this.revisionNumber = revisionNumber;
        this.delta = delta;
        
        this.requestType = UPDATE;
        this.isServerSideRequest = true;
        this.isUpdateType = false;
    }
    
    /**
     * Creates a DaapRequest from the the requestLine
     *
     * @param requestLine
     * @throw URIException
     */
    public DaapRequest(DaapConnection connection, String requestLine) 
            throws URIException {
        this(connection);
        
        String method = null;
        URI uri = null;
        String protocol = null;
        
        try {
            StringTokenizer st = new StringTokenizer(requestLine, " ");
            method = st.nextToken();
            
            try {
                uri = new URI(st.nextToken().toCharArray());
            } catch (URIException err) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(err);
                }
            }
            
            protocol = st.nextToken();
        } catch (NoSuchElementException err) {
            if (LOG.isErrorEnabled()) {
                LOG.error(err);
            }
        }
        
        this.isServerSideRequest = false;
        this.isUpdateType = false;
        
        setMethod(method);
        setURI(uri);
        setProtocol(protocol); 
    }
    
    /**
     * Creates a new DaapRequest
     *
     * @param method
     * @param uri
     * @param protocol
     * @throw URIException
     */
    public DaapRequest(DaapConnection connection, String method, 
            URI uri, String protocol) throws URIException {
        this(connection);
        
        this.isServerSideRequest = false;
        this.isUpdateType = false;
        
        setMethod(method);
        setURI(uri);
        setProtocol(protocol); 
    }
    
    /**
     * Sets the request method (GET)
     *
     * @param method
     */
    private void setMethod(String method) {
        this.method = method;
    }
    
    /**
     * Sets the protocol of the request (HTTP/1.1)
     *
     * @param protocol
     */
    private void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    /**
     * Sets and parses the URI. Note: if URIException is
     * thrown then is this Request in an inconsistent state!
     *
     * @param uri
     * @throws URIException
     */    
    private void setURI(URI uri) throws URIException {
        
        this.uri = uri;
        
        if (uri != null) {
            
            String path = uri.getPath();

            this.queryMap = DaapUtil.parseQuery(uri.getQuery());

            if (path.equals("/server-info")) {
                requestType = SERVER_INFO;
            } else if (path.equals("/content-codes")) {
                requestType = CONTENT_CODES;
            } else if (path.equals("/login")) {
                requestType = LOGIN;
            } else if (path.equals("/logout")) {
                requestType = LOGOUT;
            } else if (path.equals("/update")) {
                requestType = UPDATE;
            } else if (path.equals("/resolve")) {
                requestType = RESOLVE;
            }
            
            if (queryMap.containsKey("session-id")) {
                sessionId = SessionId.parseSessionId((String)queryMap.get("session-id"));
            }
            
            if (!SessionId.INVALID.equals(sessionId)) {

                if (queryMap.containsKey("revision-number")) {
                    revisionNumber = Integer.parseInt((String)queryMap.get("revision-number"));
                }

                if (queryMap.containsKey("delta")) {
                    delta = Integer.parseInt((String)queryMap.get("delta"));
                }
                
                if (delta > revisionNumber) {
                    throw new URIException("Delta must be less or equal to revision-number: " + delta + "/" + revisionNumber);
                }
                
                if (queryMap.containsKey("meta")) {
                    metaString = queryMap.get("meta");
                }

                isUpdateType = (delta != DaapUtil.NULL) && (delta < revisionNumber);


                // "/databases/id/items"                3 tokens
                // "/databases/id/containers"           3 tokens
                // "/databases/id/items/id.format"      4 tokens
                // "/databases/id/containers/id/items"  5 tokens
                if (path.equals("/databases")) {
                    requestType = DATABASES;

                } else if (path.startsWith("/databases")) {

                    StringTokenizer tok = new StringTokenizer(path, "/");
                    int count = tok.countTokens();

                    if (count >= 3) {
                        String token = tok.nextToken();

                        if (token.equals("databases")==false) {
                            throw new URIException("Unknown token in path: " + path + " [" + token + "]@1");
                        }

                        databaseId = DaapUtil.parseUInt((String)tok.nextToken());
                        token = tok.nextToken();

                        if (token.equals("items")) {
                            requestType = DATABASE_SONGS;
                        } else if (token.equals("containers")) {
                            requestType = DATABASE_PLAYLISTS;
                        } else {
                            throw new URIException("Unknown token in path: " + path + " [" + token + "]@2");
                        }

                        if (count == 3) {
                            // do nothing...

                        } else if (count == 4) {

                            token = (String)tok.nextToken();

                            StringTokenizer fileTokenizer = new StringTokenizer(token, ".");

                            if (fileTokenizer.countTokens()==2) {
                                itemId = DaapUtil.parseUInt(fileTokenizer.nextToken());
                                requestType = SONG;

                            } else {
                                throw new URIException("Unknown token in path: " + path + " [" + token + "]@3");
                            }

                        } else if (count == 5) {
                            containerId = DaapUtil.parseUInt((String)tok.nextToken());
                            token = (String)tok.nextToken();

                            if (token.equals("items")) {
                                requestType = PLAYLIST_SONGS;

                            } else {
                                throw new URIException("Unknown token in path: " + path + " [" + token + "@4");
                            }

                        } else {
                            throw new URIException("Unknown token in path: " + path + " [" + token + "]@5");
                        }
                    } else {
                        throw new URIException("Unknown token in path: " + path);
                    }
                }
            }
        
        } else {
            
            queryMap = null;
            metaString = null;
            isUpdateType = false;
            
            requestType = DaapUtil.NULL;
            databaseId = DaapUtil.NULL;
            containerId = DaapUtil.NULL;
            itemId = DaapUtil.NULL;
            
            sessionId = SessionId.INVALID;
            revisionNumber = DaapUtil.NULL;
            delta = DaapUtil.NULL;
        }
    }
    
    public void setSessionId(SessionId sessionId) {
        this.sessionId = sessionId;
    }
    
    /**
     * Adds an array of Headers to this requests
     * list of Headers
     *
     * @return
     */
    public void addHeaders(Header[] headers) {
        for (Header header : headers) {
            this.headers.add(header);
        }
    }
    
    /**
     * Adds a list of headers to this requests
     * list
     *
     * @return
     */
    public void addHeaders(List<? extends Header> headers) {
        if (this.headers != headers)
            this.headers.addAll(headers);
    }
    
    /**
     * Adds <code>header</code> to the list
     *
     * @return
     */
    public void addHeader(Header header) {
        this.headers.add(header);
    }
    
    /**
     * Returns the entire list of Headers
     *
     * @return
     */
    public List<Header> getHeaders() {
        return headers;
    }
    
    /**
     * Returns a Header for the key or <code>null</code> if
     * no such Header is in the list
     *
     * @return
     */
    public Header getHeader(String key) {
        
        if (headers == null) {
            return null;
        }
        
        for (Header header : headers) {
            if (header.getName().equals(key)) {
                return header;
            }
        }
        return null;
    }
    
    /**
     * Returns the Server reference
     */
    public DaapServer getServer() {
        return getConnection().getServer();
    }
    
    /**
     * Returns the associated DaapConnection
     */
    public DaapConnection getConnection() {
        return connection;
    }
    
    /**
     * Returns <code>true</code> if this is an unknown
     * request
     *
     * @return
     */
    public boolean isUnknownRequest() {
        return (requestType==DaapUtil.NULL);
    }
    
    /**
     * Returns <code>true</code> if this is a server info
     * request
     *
     * <p><i>GET /server-info HTTP/1.1</i></p> 
     *
     * @return
     */
    public boolean isServerInfoRequest() {
        return (requestType==SERVER_INFO);
    }
    
    /**
     * Returns <code>true</code> if this is a content
     * codes request
     *
     * <p><i>GET /content-codes HTTP/1.1</i></p> 
     *
     * @return
     */
    public boolean isContentCodesRequest() {
        return (requestType==CONTENT_CODES);
    }
    
    /**
     * Returns <code>true</code> if this is a login
     * request
     *
     * <p><i>GET /login HTTP/1.1</i></p> 
     *
     * @return
     */
    public boolean isLoginRequest() {
        return (requestType==LOGIN);
    }
    
    /**
     * Returns <code>true</code> if this is a logout
     * request
     *
     * <p><i>GET /logout HTTP/1.1</i></p> 
     *
     * @return
     */
    public boolean isLogoutRequest() {
        return (requestType==LOGOUT);
    }
    
    /**
     * Returns <code>true</code> if this is an update
     * request
     *
     * <p><i>GET /update HTTP/1.1</i></p> 
     *
     * @return
     */
    public boolean isUpdateRequest() {
        return (requestType==UPDATE);
    }
    
    /**
     * Returns <code>true</code> if this is a resolve
     * request <i>(not supported)</i>
     *
     * <p><i>GET /resolve HTTP/1.1</i></p> 
     *
     * @return
     */
    public boolean isResolveRequest() {
        return (requestType==RESOLVE);
    }
    
    /**
     * Returns <code>true</code> if this is a databases
     * request
     *
     * <p><i>GET /databases HTTP/1.1</i></p> 
     *
     * @return
     */
    public boolean isDatabasesRequest() {
        return (requestType==DATABASES);
    }
    
    /**
     * Returns <code>true</code> if this is a database
     * songs request
     *
     * <p><i>GET /databases/databaseId/items HTTP/1.1</i></p> 
     *
     * @return
     */
    public boolean isDatabaseSongsRequest() {
        return (requestType==DATABASE_SONGS);
    }
    
    /**
     * Returns <code>true</code> if this is a database
     * playlists request
     *
     * <p><i>GET /databases/databaseId/containers HTTP/1.1</i></p> 
     *
     * @return
     */
    public boolean isDatabasePlaylistsRequest() {
        return (requestType==DATABASE_PLAYLISTS);
    }
    
    /**
     * Returns <code>true</code> if this is a playlist
     * request
     *
     * <p><i>GET /databases/databaseId/containers/containerId/items HTTP/1.1</i></p> 
     *
     * @return
     */
    public boolean isPlaylistSongsRequest() {
        return (requestType==PLAYLIST_SONGS);
    }
    
    /**
     * Returns <code>true</code> if this is a song
     * request (stream)
     *
     * <p><i>GET /databases/databaseId/items/itemId.format HTTP/1.1</i></p> 
     *
     * @return
     */
    public boolean isSongRequest() {
        return (requestType==SONG);
    }
    
    /**
     * Returns the URI
     *
     * @return
     */
    public URI getUri() {
        return uri;
    }
    
    /**
     * Returns the sessionId
     *
     * @return
     */
    public SessionId getSessionId() {
        return sessionId;
    }
    
    /**
     * Returns the revision-number
     *
     * @return
     */
    public int getRevisionNumber() {
        return revisionNumber;
    }
    
    /**
     * What's delta? Delta is the difference between
     * the current revision of the Library (Server) 
     * and the latest revision of which iTunes (Client) 
     * knows.
     *
     * @return
     */
    public int getDelta() {
        return delta;
    }
    
    /**
     * Returns the keys of the requested meta data
     * as List. Note: this data isn't used to generate
     * a response. iTunes is very fussy about the return
     * order of some items and it would be to expensive 
     * bring the List into the correct order.
     *
     * @return
     */
    public List<String> getMeta() {
        // parse only if required...
        if (meta == null && metaString != null) {
            meta = DaapUtil.parseMeta(metaString);
            metaString = null;
        }
        
        if (meta != null) {
            return Collections.unmodifiableList(meta);
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Returns the databaseId
     *
     * @return
     */
    public long getDatabaseId() {
        return databaseId;
    }
    
    /**
     * Returns the containerId
     *
     * @return
     */
    public long getContainerId() {
        return containerId;
    }
    
    /**
     * Returns the itemId
     *
     * @return
     */
    public long getItemId() {
        return itemId;
    }
    
    /**
     * Returns <code>true</code> if databaseId is set (i.e.
     * something else than {@see DaapUtil.UNDEF_VALUE}).
     *
     * @return
     */
    public boolean isDatabaseIdSet() {
        return (databaseId != DaapUtil.NULL);
    }
    
    /**
     * Returns <code>true</code> if containerId is set (i.e.
     * something else than {@see DaapUtil.UNDEF_VALUE}).
     *
     * @return
     */
    public boolean isContainerIdSet() {
        return (containerId != DaapUtil.NULL);
    }
    
    /**
     * Returns <code>true</code> if itemId is set (i.e.
     * something else than {@see DaapUtil.UNDEF_VALUE}).
     *
     * @return
     */
    public boolean isItemIdSet() {
        return (itemId != DaapUtil.NULL);
    }
    
    /**
     * Returns the raw request time.
     *
     * @return
     */
    public int getRequestType() {
        return requestType;
    }
    
    /**
     * Returns the query of this requests URI as
     * a Map
     *
     * @return
     */
    public Map<String, String> getQueryMap() {
        if (queryMap != null) {
            return Collections.unmodifiableMap(queryMap);
        }
        
        return Collections.emptyMap();
    }
    
    /**
     * Returns <code>true</code> if this is a "fake" request 
     * generated by the server. It's needed to bypass some
     * security checks of DaapRequestProcessor.
     *
     * @return
     */
    public boolean isServerSideRequest() {
        return isServerSideRequest;
    }
    
    /**
     * Returns <code>true</code> if this request is an update
     * request. Except for the first request it's always
     * update type request.
     *
     * @return
     */
    public boolean isUpdateType() {
        return isUpdateType;
    }
    
    /**
     * Returns <code>true</code> if client accepts GZIP
     * encoding.
     * 
     * @return
     */
    public boolean isGZIPSupported() {
        Header header = getHeader("Accept-Encoding");
        return header != null && header.getValue().equalsIgnoreCase("gzip");
    }
    
    public boolean isKeepConnectionAlive() {
        Header header = getHeader("Connection");
        return header != null && header.getValue().equalsIgnoreCase("keep-alive");
    }
    
    /**
     * 
     * @return
     */
    public Library getLibrary() {
        return connection.getServer().getLibrary();
    }
    
    /**
     * 
     * @return
     */
    public Library getHeadLibrary() {
        return connection.getFirstInQueue();
    }
    
    /**
     * 
     * @return
     */
    public Library nextLibrary() {
        return connection.nextLibrary(this);
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        
        if (isServerSideRequest)
            buffer.append("ServerSideRequest: ")
                .append(getRevisionNumber()).append(", ").append(getDelta()).append("\n");
        
        if (uri != null)
            buffer.append(uri).append("\n");
        
        if (headers != null) {
            for(int i = 0; i < headers.size(); i++)
                buffer.append(headers.get(i));
        }
        
        return buffer.toString();
    }
}
