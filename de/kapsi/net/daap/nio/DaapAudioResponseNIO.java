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
import de.kapsi.net.daap.DaapRequest;
import de.kapsi.net.daap.DaapAudioResponse;
import de.kapsi.net.daap.DaapStreamException;

/**
 *
 * @author  roger
 */
public class DaapAudioResponseNIO extends DaapAudioResponse {
    
    private ByteBuffer headerBuffer;
    private FileChannel chIn;
    private SocketChannel channel;
    
    /** Creates a new instance of DaapAudioResponse */
    public DaapAudioResponseNIO(DaapRequest request, Song song, FileInputStream in, int pos, int end) throws IOException {
        super(request, song, in, pos, end);
        
        headerBuffer = ByteBuffer.wrap(header);
        
        chIn = in.getChannel();
        
        DaapConnectionNIO connection = (DaapConnectionNIO)request.getConnection();
        channel = connection.getChannel();
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

        try {
            return stream();
        } catch (IOException err) {
            throw new DaapStreamException(err);
        }
    }
    
    private boolean stream() throws IOException {
        
        if (pos < end) {
            
            if (!channel.isOpen()) {
                close();
                return true;
                
            } else {
            
                // Stream...
                try {

                    pos += chIn.transferTo(pos, 512, channel);

                    if (pos >= end) {
                        close();
                        return true;
                    } else {
                        return false;
                    }

                } catch (IOException err) {
                    close();
                    throw err;
                }
                
            }
            
        } else {
            return true;
        }
    }
    
    private void close() throws IOException {
        pos = end;
        in.close();
        chIn.close();
    }
}