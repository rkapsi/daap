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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Reads a CR LF terminated string line.
 * 
 * @author Roger Kapsi
 */
public class DaapLineReaderNIO {

    private static final char CR = '\r';
    private static final char LF = '\n';

    private StringBuffer lineBuf;
    private boolean complete;

    /** Creates a new instance of DaapLineReader */
    public DaapLineReaderNIO() {
        lineBuf = new StringBuffer();
    }

    /**
     * 
     * @return
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * 
     * @param in
     * @throws IOException
     * @return
     */
    public String read(ByteBuffer in, ReadableByteChannel channel)
            throws IOException {
        complete = false;

        if (in.remaining() > 0) {
            String line = line(in);

            if (line != null) {
                if (line.length() == 0)
                    return null;
                return line;
            }
        }

        in.clear();

        int len = channel.read(in);

        if (len < 0) {
            lineBuf = null;
            throw new IOException("Socket closed");
        }

        in.flip();

        String line = line(in);

        if (line != null) {
            if (line.length() != 0) {
                return line;
            }
        }

        return null;
    }

    private String line(ByteBuffer in) throws IOException {

        while (in.remaining() > 0 && lineBuf.length() < in.capacity()) {
            char current = (char) in.get();
            if (current == LF) {
                int length = lineBuf.length();
                if (length > 0 && lineBuf.charAt(length - 1) == CR) {
                    String line = lineBuf.toString().trim();

                    complete = (line.length() == 0);

                    lineBuf = new StringBuffer();
                    return line;
                } else {
                    lineBuf.append(current);
                }
            } else {
                lineBuf.append(current);
            }
        }

        if (lineBuf.length() >= in.capacity()) {
            lineBuf = new StringBuffer();
            throw new IOException("Header too large");
        }

        return null;
    }
}
