
package de.kapsi.net.daap;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A DAAP request. This class gets data from the client (iTunes)
 * to the DAAP server for use in <tt>DaapRequestProcessor.process</tt>
 * method.
 * 
 * @author  roger
 */
public class DaapRequest {
    
    public static final int SERVER_INFO         = 1;	//  "/server-info"
    public static final int CONTENT_CODES       = 2;	//  "/content-codes"
    public static final int LOGIN		= 3;	//  "/login"
    public static final int LOGOUT		= 4;	//  "/logout"
    public static final int UPDATE		= 5;	//  "/update"
    public static final int RESOLVE		= 6;	//  "/resolve"
    public static final int DATABASES		= 7;	//  "/databases"
    public static final int DATABASE_SONGS	= 8;	//  "/databases/databaseId/items"
    public static final int DATABASE_PLAYLISTS  = 9;	//  "/databases/databaseId/containers"
    public static final int PLAYLIST_SONGS	= 10;   //  "/databases/databaseId/containers/containerId/items"
    public static final int SONG		= 11;   //  "/databases/databaseId/items/itemId.format"
    
    private static final Log LOG = LogFactory.getLog(DaapRequest.class);
    
    private String method;
    private URI uri;
    private String protocol;
    
    private Map queryMap;
    
    private int sessionId = DaapUtil.UNDEF_VALUE;
    private int revisionNumber = DaapUtil.UNDEF_VALUE;
    private int delta = DaapUtil.UNDEF_VALUE;
    
    private ArrayList meta;
    private String metaString;
    
    private int requestType = DaapUtil.UNDEF_VALUE;
    private int databaseId = DaapUtil.UNDEF_VALUE;
    private int containerId = DaapUtil.UNDEF_VALUE;
    private int itemId = DaapUtil.UNDEF_VALUE;
    
    private ArrayList headers;
    private boolean isServerSideRequest;
    private boolean isUpdateType;
   
    private DaapConnection connection;
    
    /**
     * Create a new DaapRequest
     */
    private DaapRequest(DaapConnection connection) {
        this.connection = connection;
        headers = new ArrayList();
    }
    
    /**
     * Creates a server side fake update DaapRequest to issue an update
     *
     * @param sessionId
     * @param revisionNumber
     * @param delta
     */
    public DaapRequest(DaapConnection connection, int sessionId, int revisionNumber, int delta) {
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
    public DaapRequest(DaapConnection connection, String requestLine) throws URIException {
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
    public DaapRequest(DaapConnection connection, String method, URI uri, String protocol) throws URIException {
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
                sessionId = Integer.parseInt((String)queryMap.get("session-id"));
            }

            if (sessionId != DaapUtil.UNDEF_VALUE) {

                if (queryMap.containsKey("revision-number")) {
                    revisionNumber = Integer.parseInt((String)queryMap.get("revision-number"));
                }

                if (queryMap.containsKey("delta")) {
                    delta = Integer.parseInt((String)queryMap.get("delta"));
                }

                if (queryMap.containsKey("meta")) {
                    metaString = (String)queryMap.get("meta");
                }

                isUpdateType = (delta != DaapUtil.UNDEF_VALUE) && (delta < revisionNumber);


                // "/databases/id/items"                3 tokens
                // "/databases/id/containers"		3 tokens
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

                        databaseId = Integer.parseInt((String)tok.nextToken());
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
                                itemId = Integer.parseInt(fileTokenizer.nextToken());
                                requestType = SONG;

                            } else {
                                throw new URIException("Unknown token in path: " + path + " [" + token + "]@3");
                            }

                        } else if (count == 5) {
                            containerId = Integer.parseInt((String)tok.nextToken());
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

            this.queryMap = queryMap;
        
        } else {
            
            queryMap = null;
            metaString = null;
            isUpdateType = false;
            
            requestType = DaapUtil.UNDEF_VALUE;
            databaseId = DaapUtil.UNDEF_VALUE;
            containerId = DaapUtil.UNDEF_VALUE;
            itemId = DaapUtil.UNDEF_VALUE;
            
            sessionId = DaapUtil.UNDEF_VALUE;
            revisionNumber = DaapUtil.UNDEF_VALUE;
            delta = DaapUtil.UNDEF_VALUE;
        }
    }
    
    /**
     * Adds an array of Headers to this requests
     * list of Headers
     *
     * @return
     */
    public void addHeaders(Header[] headers) {
        for(int i = 0; i < headers.length; i++)
            this.headers.add(headers[i]);
    }
    
    /**
     * Adds a list of headers to this requests
     * list
     *
     * @return
     */
    public void addHeaders(List headers) {
        if (this.headers != headers)
            this.headers.addAll(headers);
    }
    
    /**
     * Adds <tt>header</tt> to the list
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
    public List getHeaders() {
        return headers;
    }
    
    /**
     * Returns a Header for the key or <tt>null</tt> if
     * no such Header is in the list
     *
     * @return
     */
    public Header getHeader(String key) {
        
        if (headers == null)
            return null;
        
        Iterator it = headers.iterator();
        while(it.hasNext()) {
            Header header = (Header)it.next();
            if (header.getName().equals(key)) {
                return header;
            }
        }
        
        return null;
    }
    
    /**
     * Returns the associated DaapConnection
     */
    public DaapConnection getConnection() {
        return connection;
    }
    
    /**
     * Returns <tt>true</tt> if this is an unknown
     * request
     *
     * @return
     */
    public boolean isUnknownRequest() {
        return (requestType==DaapUtil.UNDEF_VALUE);
    }
    
    /**
     * Returns <tt>true</tt> if this is a server info
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
     * Returns <tt>true</tt> if this is a content
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
     * Returns <tt>true</tt> if this is a login
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
     * Returns <tt>true</tt> if this is a logout
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
     * Returns <tt>true</tt> if this is an update
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
     * Returns <tt>true</tt> if this is a resolve
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
     * Returns <tt>true</tt> if this is a databases
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
     * Returns <tt>true</tt> if this is a database
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
     * Returns <tt>true</tt> if this is a database
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
     * Returns <tt>true</tt> if this is a playlist
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
     * Returns <tt>true</tt> if this is a song
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
    public int getSessionId() {
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
    public List getMeta() {
        // parse only if required...
        if (meta == null && metaString != null) {
            meta = DaapUtil.parseMeta(metaString);
            metaString = null;
        }
        
        return meta;
    }
    
    /**
     * Returns the databaseId
     *
     * @return
     */
    public int getDatabaseId() {
        return databaseId;
    }
    
    /**
     * Returns the containerId
     *
     * @return
     */
    public int getContainerId() {
        return containerId;
    }
    
    /**
     * Returns the itemId
     *
     * @return
     */
    public int getItemId() {
        return itemId;
    }
    
    /**
     * Returns <tt>true</tt> if databaseId is set (i.e.
     * something else than <tt>UNDEF_VALUE</tt>).
     *
     * @return
     */
    public boolean isDatabaseIdSet() {
        return (databaseId != DaapUtil.UNDEF_VALUE);
    }
    
    /**
     * Returns <tt>true</tt> if containerId is set (i.e.
     * something else than <tt>UNDEF_VALUE</tt>).
     *
     * @return
     */
    public boolean isContainerIdSet() {
        return (containerId != DaapUtil.UNDEF_VALUE);
    }
    
    /**
     * Returns <tt>true</tt> if itemId is set (i.e.
     * something else than <tt>UNDEF_VALUE</tt>).
     *
     * @return
     */
    public boolean isItemIdSet() {
        return (itemId != DaapUtil.UNDEF_VALUE);
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
    public Map getQueryMap() {
        return queryMap;
    }
    
    /**
     * Returns <tt>true</tt> if this is a "fake" request 
     * generated by the server. It's needed to bypass some
     * security checks of DaapRequestProcessor.
     *
     * @return
     */
    public boolean isServerSideRequest() {
        return isServerSideRequest;
    }
    
    /**
     * Returns <tt>true</tt> if this request is an update
     * request. Except for the first request it's always
     * update type request.
     *
     * @return
     */
    public boolean isUpdateType() {
        return isUpdateType;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append(uri).append("\n");
        
        if (headers != null) {
            for(int i = 0; i < headers.size(); i++)
                buffer.append(headers.get(i));
        }
        
        return buffer.toString();
    }
}
