/*
 * DaapAudioStream.java
 *
 * Created on April 2, 2004, 6:34 PM
 */

package de.kapsi.net.daap.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import de.kapsi.net.daap.DaapUtil;
import de.kapsi.net.daap.DaapResponse;
import de.kapsi.net.daap.DaapConnection;

/**
 *
 * @author  roger
 */
public class DaapAudioResponse implements DaapResponse {
    
    private long position;
    private long end;
   
    private DaapHeader header;
    private FileChannel in;
    private SocketChannel channel;
    
    /** Creates a new instance of DaapAudioResponse */
    public DaapAudioResponse(DaapConnection connection, FileChannel in, long position, long end) 
            throws IOException {
                
        this.in = in;
        this.position = position;
        this.end = end;
        
        channel = ((DaapConnectionImpl)connection).getChannel();
        header = DaapHeader.createAudioHeader(connection, (int)in.size());
    }
    
    public boolean hasRemainig() {
        if (header.hasRemaining())
            return true;
        else return (position < end);
    }
    
    public boolean write() throws IOException {
             
        if (header.hasRemaining()) {
            if (header.write() == false) {
                return false;
            }
        }
        
        return stream();
    }
    
    private boolean stream() throws IOException {
        
        if (position < end) {
            
            if (!channel.isOpen()) {
                close();
                return true;
            }
            
            try {
                
                position += in.transferTo(position, 512, channel);

                if (position >= end) {
                    close();
                    return true;
                }
                
                return false;
                
            } catch (IOException err) {
                close();
                throw err;
            }
        }
        
        return true;
    }
    
    private void close() throws IOException {
        position = end;
        in.close();
    }
    
    public String toString() {
        return header.toString();
    }
}