/*
 * DaapHeader.java
 *
 * Created on April 2, 2004, 1:03 PM
 */

package de.kapsi.net.daap.nio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import java.util.ArrayList;

import de.kapsi.net.daap.DaapUtil;
import de.kapsi.net.daap.DaapRequest;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author  roger
 */
public class DaapHeader {
    
    private static final Log LOG = LogFactory.getLog(DaapHeader.class);
    
    private static final byte[] CRLF = { (byte)'\r', (byte)'\n' };
    private static final String ISO_8859_1 = "ISO-8859-1";
    
    private static final String GZIP = "gzip";
    private static final String HTTP_OK = "HTTP/1.1 200 OK";
    private static final String HTTP_AUTH = "HTTP/1.1 401 Authorization Required";
    
    public static DaapHeader createChunkHeader(DaapConnection connection, int contentLength) {
        DaapHeader daapHeader = new DaapHeader();
        daapHeader.channel = connection.getChannel();
        
        try {
            
            String serverName = connection.getServer().getConfig().getServerName();
            
            Header[] hdr = {
                new Header("Date", DaapUtil.now()),
                new Header("DAAP-Server", serverName),
                new Header("Content-Type", "application/x-dmap-tagged"),
                new Header("Content-Length", Integer.toString(contentLength)),
                new Header("Content-Encoding", GZIP)
            };
            
            daapHeader.out = toByteBuffer(HTTP_OK, hdr);
            
        } catch (UnsupportedEncodingException err) {
            LOG.error(err);
        }
        
        return daapHeader;
    }
    
    public static DaapHeader createAudioHeader(DaapConnection connection, int contentLength) {
        DaapHeader daapHeader = new DaapHeader();
        daapHeader.channel = connection.getChannel();
        
        try {
            
            String serverName = connection.getServer().getConfig().getServerName();
            
            Header[] hdr = {
                new Header("Date", DaapUtil.now()),
                new Header("DAAP-Server", serverName),
                new Header("Content-Type", "application/x-dmap-tagged"),
                new Header("Content-Length", Integer.toString(contentLength)),
                new Header("Accept-Ranges", "bytes")
            };
            
            daapHeader.out = toByteBuffer(HTTP_OK, hdr);
            
        } catch (UnsupportedEncodingException err) {
            LOG.error(err);
        }
        
        return daapHeader;
    }
    
    public static DaapHeader createAuthHeader(DaapConnection connection) {
        DaapHeader daapHeader = new DaapHeader();
        daapHeader.channel = connection.getChannel();
        
        try {
            
            String serverName = connection.getServer().getConfig().getServerName();
            
            Header[] hdr = {
                new Header("Date", DaapUtil.now()),
                new Header("DAAP-Server", serverName),
                new Header("Content-Type", "text/html"),
                new Header("Content-Length", "0"),
                new Header("WWW-Authenticate", "Basic-realm=\"daap\"")
            };
            
            daapHeader.out = toByteBuffer(HTTP_AUTH, hdr);
            
        } catch (UnsupportedEncodingException err) {
            LOG.error(err);
        }
        
        return daapHeader;
    }
    
    private static ByteBuffer toByteBuffer(String statusLine, Header[] headers) 
            throws UnsupportedEncodingException {
        
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        
        buffer.put(statusLine.getBytes(ISO_8859_1)).put(CRLF);
        for(int i = 0; i < headers.length; i++) {
            buffer.put(headers[i].toExternalForm().getBytes(ISO_8859_1));
        }
        buffer.put(CRLF);
        buffer.flip();
        
        return buffer;
    }
    
    private SocketChannel channel;
    private ByteBuffer out;
    
    /** Creates a new instance of DaapHeader */
    private DaapHeader() {
        
    }
    
    public boolean hasRemaining() {
        
        if (out.remaining()==0) {
            return false;
        }
        
        return true;
    }
    
    public boolean write() throws IOException {
        if (hasRemaining()) {
            channel.write(out);
            return hasRemaining();
        }
        return true;
    }
    
    public String toString() {
        return (new String(out.array()));
    }
}
