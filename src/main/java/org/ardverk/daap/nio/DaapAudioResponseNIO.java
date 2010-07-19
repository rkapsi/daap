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

package org.ardverk.daap.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.ardverk.daap.DaapAudioResponse;
import org.ardverk.daap.DaapConfig;
import org.ardverk.daap.DaapRequest;
import org.ardverk.daap.DaapStreamException;
import org.ardverk.daap.Song;

/**
 * DaapAudioResponse.
 * 
 * @author Roger Kapsi
 */
public class DaapAudioResponseNIO extends DaapAudioResponse {

    private ByteBuffer headerBuffer;
    private FileChannel fileChannel;
    private DaapConnectionNIO connection;

    public DaapAudioResponseNIO(DaapRequest request, Song song, File file,
            long pos, long end) throws IOException {
        this(request, song, new FileInputStream(file), pos, end);
    }

    /** Creates a new instance of DaapAudioResponse */
    public DaapAudioResponseNIO(DaapRequest request, Song song,
            FileInputStream in, long pos, long end) {
        super(request, song, in, pos, end);

        headerBuffer = ByteBuffer.wrap(header);
        this.connection = (DaapConnectionNIO) request.getConnection();

        fileChannel = in.getChannel();
    }

    public boolean hasRemaining() {
        if (headerBuffer.hasRemaining())
            return true;
        else
            return (pos < end);
    }

    public boolean write() throws IOException {

        if (headerBuffer.hasRemaining()) {

            try {

                connection.getWriteChannel().write(headerBuffer);

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

            if (!connection.getWriteChannel().isOpen()) {
                close();
                return true;

            } else {

                // Stream...
                try {
                    DaapConfig config = request.getServer().getConfig();
                    pos += fileChannel.transferTo(pos, config.getBufferSize(),
                            connection.getWriteChannel());

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

    protected void close() throws IOException {
        super.close();
        fileChannel.close();
    }
}
