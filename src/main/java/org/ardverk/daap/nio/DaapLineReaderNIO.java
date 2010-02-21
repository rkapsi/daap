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

package org.ardverk.daap.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Reads a CR LF terminated string line.
 * 
 * @author  Roger Kapsi
 */
public class DaapLineReaderNIO {
    
    private static final char CR = '\r';
    private static final char LF = '\n';
    
    private StringBuffer lineBuf;
    private boolean complete;
   
    /** Creates a new instance of DaapLineReader */
    public DaapLineReaderNIO() {
        lineBuf = new StringBuffer();
    }
    
    /**
     *
     * @return
     */    
    public boolean isComplete() {
        return complete;
    }
    
    /**
     *
     * @param in
     * @throws IOException
     * @return
     */    
    public String read(ByteBuffer in, ReadableByteChannel channel) throws IOException {
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
            if (current == LF) {
                int length = lineBuf.length();
                if (length > 0 && lineBuf.charAt(length-1) == CR) {
                    String line = lineBuf.toString().trim();
                    
                    complete = (line.length() == 0);
                    
                    lineBuf = new StringBuffer();
                    return line;
                } else {
                    lineBuf.append(current);
                }
            } else {
                lineBuf.append(current);
            }
        }
        
        if (lineBuf.length() >= in.capacity()) {
            lineBuf = new StringBuffer();
            throw new IOException("Header too large");
        }
        
        return null;
    }
}
