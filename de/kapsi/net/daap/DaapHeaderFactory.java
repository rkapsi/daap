/*
 * DaapHeaderFactory.java
 *
 * Created on April 2, 2004, 6:07 PM
 */

package de.kapsi.net.daap;

import de.kapsi.net.daap.DaapUtil;
import org.apache.commons.httpclient.Header;

/**
 *
 * @author  roger
 */
public final class DaapHeaderFactory {
    
    private static final String GZIP = "gzip";
    
    public static final String HTTP_OK = "HTTP/1.1 200 OK";
    public static final String HTTP_AUTH = "HTTP/1.1 401 Authorization Required";
    
    /** Creates a new instance of DaapHeaderFactory */
    private DaapHeaderFactory() {
    }
    
    public static Header[] createChunkHeaders(String serverName, int contentLength) {
        return createChunkHeaders(serverName, contentLength, GZIP);
    }
    
    public static Header[] createChunkHeaders(String serverName, int contentLength, String encoding) {
        
        Header[] headers = {
            new Header("Date", DaapUtil.now()),
            new Header("DAAP-Server", serverName),
            new Header("Content-Type", "application/x-dmap-tagged"),
            new Header("Content-Length", Integer.toString(contentLength)),
            new Header("Content-Encoding", encoding)
        };
        
        return headers;
    }
    
    public static Header[] createAuthHeaders(String serverName) {
        
        Header[] headers = {
            new Header("Date", DaapUtil.now()),
            new Header("DAAP-Server", serverName),
            new Header("Content-Type", "text/html"),
            new Header("Content-Length", "0"),
            new Header("WWW-Authenticate", "Basic-realm=\"daap\"")
        };
        
        return headers;
    }
    
    public static Header[] createAudioHeaders(String serverName, int contentLength) {
        
        Header[] headers = {
            new Header("Date", DaapUtil.now()),
            new Header("DAAP-Server", serverName),
            new Header("Content-Type", "application/x-dmap-tagged"),
            new Header("Content-Length", Integer.toString(contentLength)),
            new Header("Accept-Ranges", "bytes")
        };
        
        return headers;
    }
}
