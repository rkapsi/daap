/*
 * HttpLineReader.java
 *
 * Created on March 31, 2004, 10:14 PM
 */

package de.kapsi.net.daap.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 *
 * @author  roger
 */
public class DaapLineReader {
    
    private static final char CR = '\r';
    private static final char LF = '\n';
    
    private SocketChannel channel;
    
    private StringBuffer lineBuf;
    private int capacity;
    private boolean complete;
   
    /** Creates a new instance of DaapLineReader */
    public DaapLineReader(SocketChannel channel) {
        this.channel = channel;
        lineBuf = new StringBuffer();
    }
    
    public boolean isComplete() {
        return complete;
    }
    
    public String read(ByteBuffer in) throws IOException {
        
        complete = false;
        
        if (in.remaining() > 0) {
            String line = line(in);
            
            if (line != null) {
                if (line.length() == 0)
                    return null;
                return line;
            }
        }
        
        in.clear();
        
        int len = channel.read(in);
        
        if (len < 0) {
            lineBuf = null;
            in = null;
            throw new IOException("Socket closed");
        }
        
        in.flip();
        
        String line = line(in);
            
        if (line != null) {
            if (line.length() != 0) {
                return line;
            }
        }
        
        return null;
    }
    
    private String line(ByteBuffer in) throws IOException {
        
        while(in.remaining() > 0 && lineBuf.length() < in.capacity()) {
            char current = (char)in.get();
            if (current == CR) {
                char next = (char)in.get();
                if (next == LF) {
                    
                    String line = lineBuf.toString().trim();
                    
                    complete = (line.length() == 0);
                    
                    lineBuf = new StringBuffer();
                    return line;
                    
                } else {
                    
                    lineBuf.append(current);
                    lineBuf.append(next);
                }
            } else {
                lineBuf.append(current);
            }
        }
        
        return null;
    }
}
