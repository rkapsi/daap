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

package org.ardverk.daap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This Factory interface is used to create either NIO
 * or BIO based DaapResponses.
 *
 * @author  Roger Kapsi
 */
public interface DaapResponseFactory {
    
    /**
     * 
     * @param request
     * @return
     */    
    public DaapResponse createNoContentResponse(DaapRequest request);
    
    
    /**
     * Creates an returns a DaapAuthResponse for the passed request.
     *
     * @param request
     * @return
     */    
    public DaapResponse createAuthResponse(DaapRequest request);
    
    /**
     * Creates and returns a DaapChunResponse for the passed request and
     * data (payload, i.e. the serialized Chunks).
     *
     * @param request
     * @param data
     * @return
     */    
    public DaapResponse createChunkResponse(DaapRequest request, byte[] data);
    
    /**
     * Creates and returns a DaapAudioResponse for the passed parameters.
     * 
     * @param request
     * @param song
     * @param in
     * @param pos
     * @param end
     * @return
     * @throws IOException
     */ 
    public DaapResponse createAudioResponse(DaapRequest request, Song song, File file, long pos, long end) throws IOException;
    
    /**
     * Creates and returns a DaapAudioResponse for the passed parameters.
     * 
     * @param request
     * @param song
     * @param in
     * @param pos
     * @param end
     * @return
     * @throws IOException
     */ 
    public DaapResponse createAudioResponse(DaapRequest request, Song song, FileInputStream in, long pos, long end) throws IOException;
}
