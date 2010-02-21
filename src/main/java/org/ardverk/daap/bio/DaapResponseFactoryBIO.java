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

package org.ardverk.daap.bio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.ardverk.daap.DaapRequest;
import org.ardverk.daap.DaapResponse;
import org.ardverk.daap.DaapResponseFactory;
import org.ardverk.daap.Song;

/**
 * This factory creates BIO DaapRespones.
 *
 * @author  Roger Kapsi
 */
class DaapResponseFactoryBIO implements DaapResponseFactory {
    
    /** Creates a new instance of DaapResponseFactoryBIO */
    protected DaapResponseFactoryBIO() {
    }
    
    public DaapResponse createAudioResponse(DaapRequest request, Song song, File file, long pos, long end) throws IOException {
        return new DaapAudioResponseBIO(request, song, file, pos, end);
    }
    
    public DaapResponse createAudioResponse(DaapRequest request, Song song, FileInputStream in, long pos, long end) throws IOException {
        return new DaapAudioResponseBIO(request, song, in, pos, end);
    }
    
    public DaapResponse createAuthResponse(DaapRequest request) {
        return new DaapAuthResponseBIO(request);
    }
    
    public DaapResponse createChunkResponse(DaapRequest request, byte[] data) {
        return new DaapChunkResponseBIO(request, data);
    }
    
    public DaapResponse createNoContentResponse(DaapRequest request) {
        return new DaapNoContentResponseBIO(request);
    }
}
