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

package de.kapsi.net.daap.bio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import de.kapsi.net.daap.DaapAudioResponse;
import de.kapsi.net.daap.DaapRequest;
import de.kapsi.net.daap.DaapStreamException;
import de.kapsi.net.daap.Song;

/**
 * An Audio Response.
 *
 * @author  Roger Kapsi
 */
public class DaapAudioResponseBIO extends DaapAudioResponse {
    
    private boolean headerWritten = false;
    private boolean audioWritten = false;
    
    private OutputStream out;
    
    /** Creates a new instance of DaapAudioResponse */
    public DaapAudioResponseBIO(DaapRequest request, Song song, File file, long pos, long end) throws IOException {
        this(request, song, new FileInputStream(file), pos, end);
    }
    
    /** Creates a new instance of DaapAudioResponse */
    public DaapAudioResponseBIO(DaapRequest request, Song song, FileInputStream in, long pos, long end) {
        super(request, song, in, pos, end);
        
        DaapConnectionBIO connection = (DaapConnectionBIO)request.getConnection();
        out = connection.getOutputStream();
    }
    
    public boolean hasRemaining() {
        return !(headerWritten && audioWritten);
    }
    
    public boolean write() throws IOException {
        
        try {
            
            if (!headerWritten) {

                try {

                    out.write(header, 0, header.length);
                    out.flush();
                    headerWritten = true;

                } catch (IOException err) {
                    throw err;
                }
            }

            return stream();
            
        } catch (SocketException err) {
            throw new DaapStreamException(err);
        } finally {
            close();
        }
    }
    
    private boolean stream() throws IOException {
        
        // DO NOT SET THIS TOO HIGH AS IT CAUSES RE-BUFFERING
        // AT THE BEGINNING OF HIGH BIT RATE SONGS (WAV AND AIFF) !!!
        byte[] buffer = new byte[512];
        
        int total = 0;
        int len = -1;
        
        if (pos != 0) {
            in.skip(pos);
        }
        
        while((len = in.read(buffer, 0, buffer.length)) != -1 && total < end) {
            out.write(buffer, 0, len);
            
            // DO NOT FLUSH AS IT CAUSES RE-BUFFERING AT THE
            // BEGINNING OF HIGH BIT RATE SONGS (WAV AND AIFF) !!!
            
            total += len;
        }
        
        out.flush();
        return true;
    }
    
    protected void close() throws IOException {
        super.close();
    }
}
