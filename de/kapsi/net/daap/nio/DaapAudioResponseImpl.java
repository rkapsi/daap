/*
 * DaapAudioStream.java
 *
 * Created on April 2, 2004, 6:34 PM
 */

package de.kapsi.net.daap.nio;

import java.io.IOException;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import de.kapsi.net.daap.DaapUtil;
import de.kapsi.net.daap.Song;
import de.kapsi.net.daap.DaapAudioResponse;
import de.kapsi.net.daap.DaapConnection;

/**
 *
 * @author  roger
 */
public class DaapAudioResponseImpl extends DaapAudioResponse {
    
    private ByteBuffer headerBuffer;
    private FileChannel chIn;
    private SocketChannel channel;
    
    /** Creates a new instance of DaapAudioResponse */
    public DaapAudioResponseImpl(DaapConnection connection, Song song, FileInputStream in, int pos, int end) throws IOException {
        super(connection, song, in, pos, end);
        
        headerBuffer = ByteBuffer.wrap(header);
        
        chIn = in.getChannel();
        channel = ((DaapConnectionImpl)connection).getChannel();
    }
    
    public boolean hasRemainig() {
        if (headerBuffer.hasRemaining())
            return true;
        else return (pos < end);
    }
    
    public boolean write() throws IOException {
             
        if (headerBuffer.hasRemaining()) {
            
            try {
                
                channel.write(headerBuffer);
            
                if (headerBuffer.hasRemaining() == true) {
                    return false;
                }
                
            } catch (IOException err) {
                close();
                throw err;
            }
        }
        
        return stream();
    }
    
    private boolean stream() throws IOException {
        
        if (pos < end) {
            
            if (!channel.isOpen()) {
                close();
                return true;
            }
            
            try {
                
                pos += chIn.transferTo(pos, 512, channel);

                if (pos >= end) {
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
        pos = end;
        in.close();
        chIn.close();
    }
}