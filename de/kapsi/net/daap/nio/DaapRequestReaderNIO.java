/*
 * DaapHeaderReader.java
 *
 * Created on March 31, 2004, 10:53 PM
 */

package de.kapsi.net.daap.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import java.util.ArrayList;
import java.util.LinkedList;

import de.kapsi.net.daap.DaapRequest;

import org.apache.commons.httpclient.Header;

/**
 *
 * @author  roger
 */
public class DaapRequestReaderNIO {
    
    private long bytesRead = 0;
    
    private ByteBuffer in;
    
    private String requestLine;
    private ArrayList headers;
    
    private DaapLineReaderNIO lineReader;
    
    private LinkedList pending;
    
    /** Creates a new instance of DaapRequestReader */
    public DaapRequestReaderNIO(SocketChannel channel) {
        
        in = ByteBuffer.allocate(1024);
        in.clear();
        in.flip();
        
        lineReader = new DaapLineReaderNIO(channel);
        headers = new ArrayList();
        pending = new LinkedList();
    }

    public long getBytesRead() {
        return bytesRead; 
    }
    
    public DaapRequest read() throws IOException {
        
        DaapRequest ret = null;
        
        if (pending.isEmpty()==false)
            ret = (DaapRequest)pending.removeFirst();
        
        String line = null;
        
        while((line = lineReader.read(in)) != null) {
            
            bytesRead += in.position();
            
            if (requestLine == null) {
                requestLine = line;
                
            } else {
                int p = line.indexOf(':');
                
                if (p == -1) {
                    requestLine = null;
                    headers.clear();
                    lineReader = null;
                    throw new IOException("Malformed Header");
                }
                
                String name = line.substring(0, p).trim();
                String value = line.substring(++p).trim();
                headers.add(new Header(name, value));
            }
        }
        
        if (lineReader.isComplete()) {
            
            DaapRequest request = null;
            
            try {
                request = new DaapRequest(requestLine);
                request.addHeaders(headers);
            } finally {
                requestLine = null;
                headers.clear();
            }
            
            if (ret == null) {
                ret = request;
            } else {
                pending.addLast(request);
            }
        }
        
        return ret;
    }
}
