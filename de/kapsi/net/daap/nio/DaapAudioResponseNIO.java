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
 * NIO (New or Non-Blocking I/O) based DaapAudioResponse.
 *
 * @author  Roger Kapsi
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
