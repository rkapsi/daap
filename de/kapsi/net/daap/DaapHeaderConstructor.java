/*
 * DaapHeaderFactory.java
 *
 * Created on April 5, 2004, 8:30 PM
 */

package de.kapsi.net.daap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;

import java.util.ArrayList;

import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A helper class to create easily misc DAAP response header
 *
 * @author  roger
 */
public class DaapHeaderConstructor {
    
    private static final Log LOG = LogFactory.getLog(DaapHeaderConstructor.class);
    
    private static final byte[] CRLF = { (byte)'\r', (byte)'\n' };
    private static final String ISO_8859_1 = "ISO-8859-1";
    
    private static final String HTTP_OK = "HTTP/1.1 200 OK";
    private static final String HTTP_AUTH = "HTTP/1.1 401 Authorization Required";
    private static final String HTTP_PARTIAL_CONTENT = "HTTP/1.1 206 Partial Content";
    /**
     * Creates a new Chunk Header
     *
     * @param request
     * @param contentLength
     * @return
     */    
    public static byte[] createChunkHeader(DaapRequest request, int contentLength) {
        
        try {
            
            DaapConnection connection = request.getConnection();
            String serverName = connection.getServer().getConfig().getServerName();
            
            Header[] headers = {
                new Header("Date", DaapUtil.now()),
                new Header("DAAP-Server", serverName),
                new Header("Content-Type", "application/x-dmap-tagged"),
                new Header("Content-Length", Integer.toString(contentLength)),
                new Header("Content-Encoding", "gzip")
            };
            
            return toByteArray(HTTP_OK, headers);
            
        } catch (UnsupportedEncodingException err) {
            // Should never happen
            throw new RuntimeException(err);
            
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
    public static byte[] createAudioHeader(DaapRequest request, int pos, int end, int contentLength) {

        try {
            
            DaapConnection connection = request.getConnection();
            int version = connection.getProtocolVersion();
            
            if (version == DaapUtil.UNDEF_VALUE)
                throw new IOException("Client Protocol Version is unknown");
            
            String serverName = connection.getServer().getConfig().getServerName();
            
            String statusLine = null;
            
            ArrayList headers = new ArrayList();
            
            headers.add(new Header("Date", DaapUtil.now()));
            headers.add(new Header("DAAP-Server", serverName));
            headers.add(new Header("Content-Type", "application/x-dmap-tagged"));
            
            // 
            if ( version <= DaapUtil.VERSION_2 || pos == 0) {
                
                statusLine = HTTP_OK;
                headers.add(new Header("Content-Length", Integer.toString(contentLength)));
            
            } else {
                
                statusLine = HTTP_PARTIAL_CONTENT;
                
                String cotentLengthStr = Integer.toString(contentLength - pos);
                String contentRange = "bytes " + pos + "-" + (contentLength-1) + "/" + contentLength;
                headers.add(new Header("Content-Length", cotentLengthStr));
                headers.add(new Header("Content-Range", contentRange));
            }
            
            headers.add(new Header("Accept-Ranges", "bytes"));
            
            return toByteArray(statusLine, (Header[])headers.toArray(new Header[0]));
            
        } catch (UnsupportedEncodingException err) {
            // Should never happen
            throw new RuntimeException(err);
            
        } catch (IOException err) {
            // Should never happen
            throw new RuntimeException(err);
        }
    }
    
    /**
     * Creates a new Authentication Header
     *
     * @param request
     * @return
     */    
    public static byte[] createAuthHeader(DaapRequest request) {
        
        try {
            
            DaapConnection connection = request.getConnection();
            String serverName = connection.getServer().getConfig().getServerName();
            
            Header[] headers = {
                new Header("Date", DaapUtil.now()),
                new Header("DAAP-Server", serverName),
                new Header("Content-Type", "text/html"),
                new Header("Content-Length", "0"),
                new Header("WWW-Authenticate", "Basic-realm=\"daap\"")
            };
            
            return toByteArray(HTTP_AUTH, headers);
            
        } catch (UnsupportedEncodingException err) {
            // Should never happen
            throw new RuntimeException(err);
            
        } catch (IOException err) {
            // Should never happen
            throw new RuntimeException(err);
        }
    }
   
    /**
     * Converts statusLine and headers to an byte-Array
     */
    private static byte[] toByteArray(String statusLine, Header[] headers) 
            throws UnsupportedEncodingException, IOException {
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        out.write(statusLine.getBytes(ISO_8859_1));
        out.write(CRLF);
        
        for(int i = 0; i < headers.length; i++) {
            out.write(headers[i].toExternalForm().getBytes(ISO_8859_1));
        }
        
        out.write(CRLF);
        out.close();
        
        return out.toByteArray();
    }
    
    /** Creates a new instance of DaapHeaderFactory */
    private DaapHeaderConstructor() {
    }
}
    
    