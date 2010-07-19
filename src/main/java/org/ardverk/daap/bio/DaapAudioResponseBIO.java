/*
 * Digital Audio Access Protocol (DAAP) Library
 * Copyright (C) 2004-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.daap.bio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import org.ardverk.daap.DaapAudioResponse;
import org.ardverk.daap.DaapRequest;
import org.ardverk.daap.DaapStreamException;
import org.ardverk.daap.Song;

/**
 * An Audio Response.
 * 
 * @author Roger Kapsi
 */
public class DaapAudioResponseBIO extends DaapAudioResponse {

    private boolean headerWritten = false;
    private boolean audioWritten = false;

    private OutputStream out;

    /** Creates a new instance of DaapAudioResponse */
    public DaapAudioResponseBIO(DaapRequest request, Song song, File file,
            long pos, long end) throws IOException {
        this(request, song, new FileInputStream(file), pos, end);
    }

    /** Creates a new instance of DaapAudioResponse */
    public DaapAudioResponseBIO(DaapRequest request, Song song,
            FileInputStream in, long pos, long end) {
        super(request, song, in, pos, end);

        DaapConnectionBIO connection = (DaapConnectionBIO) request
                .getConnection();
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

        while ((len = in.read(buffer, 0, buffer.length)) != -1 && total < end) {
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
