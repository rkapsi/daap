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
import java.nio.channels.SocketChannel;

import de.kapsi.net.daap.DaapResponse;
import de.kapsi.net.daap.DaapRequest;
import de.kapsi.net.daap.DaapAuthResponse;

/**
 * NIO (New or Non-Blocking I/O) based DaapAuthResponse.
 *
 * @author  Roger Kapsi
 */
public class DaapAuthResponseNIO extends DaapAuthResponse {
    
    private ByteBuffer headerBuffer;
    private SocketChannel channel;
    
    /** Creates a new instance of DaapAuthResponse */
    public DaapAuthResponseNIO(DaapRequest request) {
        super(request);
        
        DaapConnectionNIO connection = (DaapConnectionNIO)request.getConnection();
        channel = connection.getChannel();
        
        headerBuffer = ByteBuffer.wrap(header);
    }
    
    
    public boolean hasRemainig() {
        return headerBuffer.hasRemaining();
    }
    
    
    /**
     *
     * @throws IOException
     * @return
     */    
    public boolean write() throws IOException {
        if (headerBuffer.hasRemaining()) {
            channel.write(headerBuffer);
            return !headerBuffer.hasRemaining();
        }
        
        return true;
    }
}
