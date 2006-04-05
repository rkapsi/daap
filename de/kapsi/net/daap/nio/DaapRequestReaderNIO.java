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

package de.kapsi.net.daap.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.commons.httpclient.Header;

import de.kapsi.net.daap.DaapRequest;

/**
 * This class reads bytes from a SocketChannel and 
 * constructs a DaapRequest of the data...
 *
 * @author  Roger Kapsi
 */
class DaapRequestReaderNIO {
    
    // The max size of a header row in bytes
    private static final int MAX_HEADER_SIZE = 4096;
    
    private long bytesRead = 0;
    
    private DaapConnectionNIO connection;
    private ByteBuffer in;
    
    private String requestLine;
    private ArrayList headers;
    
    private DaapLineReaderNIO lineReader;
    
    private LinkedList pending;
    
    /** Creates a new instance of DaapRequestReader */
    DaapRequestReaderNIO(DaapConnectionNIO connection) {
        
        this.connection = connection;
        
        in = ByteBuffer.allocate(MAX_HEADER_SIZE);
        in.clear();
        in.flip();
        
        lineReader = new DaapLineReaderNIO();
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
        
        while((line = lineReader.read(in, connection.getReadChannel())) != null) {
            
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
                request = new DaapRequest(connection, requestLine);
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
        } else if (headers.size() >= 64) {
            requestLine = null;
            headers.clear();
            throw new IOException("Header too large");
        }
        
        return ret;
    }
}
