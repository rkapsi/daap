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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

/**
 * A helper class to create easily misc DAAP response header
 *
 * @author  Roger Kapsi
 */
public class DaapHeaderConstructor {
    
    //private static final Logger LOG = LoggerFactory.getLogger(DaapHeaderConstructor.class);
    
    private static final String HTTP_OK = "HTTP/1.1 200 OK";
    private static final String HTTP_NO_CONTENT = "HTTP/1.1 204 No Content";
    private static final String HTTP_PARTIAL_CONTENT = "HTTP/1.1 206 Partial Content";
    private static final String HTTP_AUTH = "HTTP/1.1 401 Authorization Required";
    
    //private static final String BAD_REQUEST = "HTTP/1.1 400 Bad Request";
    //private static final String HTTP_FORBIDDEN = "HTTP/1.1 403 Forbidden";
    //private static final String HTTP_SERVICE_UNAVAILABLE = "HTTP/1.1 503 Service Unavailable";
    
    /**
     * Creates a new Chunk Header
     *
     * @param request
     * @param contentLength
     * @return
     */    
    public static byte[] createChunkHeader(DaapRequest request, long contentLength) {
        
        /*if (request.isLoginRequest()) {
            return createServiceUnavialable(request);
        }*/

        try {
            
            DaapConnection connection = request.getConnection();
            String serverName = connection.getServer().getConfig().getServerName();
            
            List<Header> headers = new ArrayList<Header>();
            headers.add(new BasicHeader("Date", DaapUtil.now()));
            headers.add(new BasicHeader("DAAP-Server", serverName));
            headers.add(new BasicHeader("Content-Type", "application/x-dmap-tagged"));
            headers.add(new BasicHeader("Content-Length", Long.toString(contentLength)));
            headers.add(new BasicHeader("Connection", "Keep-Alive"));
            
            if (DaapUtil.COMPRESS && request.isGZIPSupported()) {
                headers.add(new BasicHeader("Content-Encoding", "gzip"));
            }
            
            return toByteArray(HTTP_OK, headers.toArray(new Header[0]));
            
        } catch (IOException err) {
            // Should never happen
            throw new RuntimeException(err);
        }
    }
    
    /**
     * Creates an Audio Header
     *
     * @param request
     * @param contentLength
     * @return
     */    
    public static byte[] createAudioHeader(DaapRequest request, long pos, long end, long contentLength) {
        
        try {
            
            DaapConnection connection = request.getConnection();
            int version = connection.getProtocolVersion();
            
            if (version == DaapUtil.NULL)
                throw new IOException("Client Protocol Version is unknown");
            
            String serverName = connection.getServer().getConfig().getServerName();
            
            String statusLine = null;
            
            List<Header> headers = new ArrayList<Header>();
            headers.add(new BasicHeader("Date", DaapUtil.now()));
            headers.add(new BasicHeader("DAAP-Server", serverName));
            headers.add(new BasicHeader("Content-Type", "application/x-dmap-tagged"));
            headers.add(new BasicHeader("Connection", "close"));
            
            // 
            if (pos == 0 || version <= DaapUtil.DAAP_VERSION_2 ) {
                
                statusLine = HTTP_OK;
                headers.add(new BasicHeader("Content-Length", Long.toString(contentLength)));
            
            } else {
                
                statusLine = HTTP_PARTIAL_CONTENT;
                
                String cotentLengthStr = Long.toString(contentLength - pos);
                String contentRange = "bytes " + pos + "-" + (contentLength-1) + "/" + contentLength;
                headers.add(new BasicHeader("Content-Length", cotentLengthStr));
                headers.add(new BasicHeader("Content-Range", contentRange));
            }
            
            headers.add(new BasicHeader("Accept-Ranges", "bytes"));
            
            return toByteArray(statusLine, headers.toArray(new Header[0]));
            
        } catch (IOException err) {
            // Should never happen
            throw new RuntimeException(err);
        }
    }
    
    /**
     * Creates a new Basic Authentication Header
     *
     * @param request
     * @return
     */    
    public static byte[] createBasicAuthHeader(DaapRequest request) {
        
        try {
            
            DaapConnection connection = request.getConnection();
            String serverName = connection.getServer().getConfig().getServerName();
            
            Header[] headers = {
                new BasicHeader("Date", DaapUtil.now()),
                new BasicHeader("DAAP-Server", serverName),
                new BasicHeader("Content-Type", "text/html"),
                new BasicHeader("Content-Length", "0"),
                new BasicHeader("WWW-Authenticate", "Basic realm=\"" + DaapUtil.DAAP_REALM + "\""),
                new BasicHeader("Connection", "Keep-Alive")
            };
            
            return toByteArray(HTTP_AUTH, headers);
            
        } catch (IOException err) {
            // Should never happen
            throw new RuntimeException(err);
        }
    }
    
    /**
     * Creates a Digest Authentication Header
     */
    public static byte[] createDigestAuthHeader(DaapRequest request) {
        try {
            
            DaapConnection connection = request.getConnection();
            String serverName = connection.getServer().getConfig().getServerName();
            String nonce = connection.createNonce();
            
            Header[] headers = {
                new BasicHeader("Date", DaapUtil.now()),
                new BasicHeader("DAAP-Server", serverName),
                new BasicHeader("Content-Type", "text/html"),
                new BasicHeader("Content-Length", "0"),
                new BasicHeader("WWW-Authenticate", "Digest realm=\"" + DaapUtil.DAAP_REALM + "\", nonce=\"" + nonce + "\""),
                new BasicHeader("Connection", "Keep-Alive")
            };
            
            return toByteArray(HTTP_AUTH, headers);
            
        } catch (IOException err) {
            // Should never happen
            throw new RuntimeException(err);
        }
    }
    
    /**
     * Creates a new No Content Header
     *
     * @param request
     * @return
     */    
    public static byte[] createNoContentHeader(DaapRequest request) {
        
        try {
            
            DaapConnection connection = request.getConnection();
            String serverName = connection.getServer().getConfig().getServerName();

            Header[] headers = {
                new BasicHeader("Date", DaapUtil.now()),
                new BasicHeader("DAAP-Server", serverName),
                new BasicHeader("Content-Type", "application/x-dmap-tagged"),
                new BasicHeader("Content-Length", "0"),
                new BasicHeader("Connection", request.isLogoutRequest() ? "close" : "Keep-Alive")
            };
            
            return toByteArray(HTTP_NO_CONTENT, headers);
            
        } catch (IOException err) {
            // Should never happen
            throw new RuntimeException(err);
        }
    }
    
    /*public static byte[] createForbidden(DaapRequest request) {
        try {
            
            DaapServer server = request.getServer();
            DaapConfig config = server.getConfig();
            String serverName = config.getServerName();
            
            Header[] headers = {
                new Header("Date", DaapUtil.now()),
                new Header("DAAP-Server", serverName),
                new Header("Content-Type", "application/x-dmap-tagged"),
                new Header("Content-Length", "0")
            };
            
            return toByteArray(HTTP_FORBIDDEN, headers);
            
        } catch (IOException err) {
            // Should never happen
            throw new RuntimeException(err);
        }
    }*/

    /**
     * As a response to /login this will produce two types of
     * error messages on the client side.
     * 
     * 1) if max connections is equal to iTunes five unique 
     * connections per day limit you'll get the following message:
     * 
     * "The shared music library "The WIRED CD" accepts 
     * only five different users each day. Please try again
     * later."
     * 
     * 2) if max connections is something else you'll get this
     * message:
     * 
     * "The shared music library "The WIRED CD" is not 
     * accepting connections at this time. Please try again
     * later."
     */
    /*public static byte[] createServiceUnavialable(DaapRequest request) {
        try {
            
            DaapServer server = request.getServer();
            DaapConfig config = server.getConfig();
            String serverName = config.getServerName();
            
            // Contents is the max number of connections 
            // encoded as binary string.
            
            String max = "";
            if (config.getMaxConnections() > 0) {
                max = Integer.toBinaryString(config.getMaxConnections());
            }
            
            byte[] maxBinString = DaapUtil.getBytes(max, DaapUtil.ISO_8859_1);
            
            Header[] headers = {
                new Header("Date", DaapUtil.now()),
                new Header("DAAP-Server", serverName),
                new Header("Content-Type", "text/html"),
                new Header("Content-Length", Integer.toString(maxBinString.length))
            };
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            toByteArray(out, HTTP_SERVICE_UNAVAILABLE, headers);
            out.write(maxBinString);
            out.close();
            return out.toByteArray();
            
        } catch (IOException err) {
            // Should never happen
            throw new RuntimeException(err);
        }
    }*/
    
    /**
     * Converts statusLine and headers to an byte-Array
     */
    private static byte[] toByteArray(String statusLine, Header[] headers) 
            throws IOException {
       
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        toByteArray(out, statusLine, headers).close();
        return out.toByteArray();
    }
    
    /**
     * Converts statusLine and headers to an byte-Array
     */
    private static OutputStream toByteArray(OutputStream out, String statusLine, Header[] headers) 
            throws IOException {
        
        out.write(DaapUtil.getBytes(statusLine, DaapUtil.ISO_8859_1));
        out.write(DaapUtil.CRLF);
        
        for(int i = 0; i < headers.length; i++) {
            out.write(DaapUtil.getBytes(headers[i].toString() + "\r\n", DaapUtil.ISO_8859_1));
        }
        
        out.write(DaapUtil.CRLF);
        return out;
    }
    
    /** Creates a new instance of DaapHeaderConstructor */
    private DaapHeaderConstructor() {
    }
}
    
    
