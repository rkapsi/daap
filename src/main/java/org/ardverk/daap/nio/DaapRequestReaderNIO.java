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
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.ardverk.daap.DaapRequest;

/**
 * This class reads bytes from a SocketChannel and constructs a DaapRequest of
 * the data...
 * 
 * @author Roger Kapsi
 */
class DaapRequestReaderNIO {

    // The max size of a header row in bytes
    private static final int MAX_HEADER_SIZE = 4096;

    private long bytesRead = 0;

    private DaapConnectionNIO connection;
    private ByteBuffer in;

    private String requestLine;
    private List<Header> headers;

    private DaapLineReaderNIO lineReader;

    private LinkedList<DaapRequest> pending;

    /** Creates a new instance of DaapRequestReader */
    DaapRequestReaderNIO(DaapConnectionNIO connection) {

        this.connection = connection;

        in = ByteBuffer.allocate(MAX_HEADER_SIZE);
        in.clear();
        in.flip();

        lineReader = new DaapLineReaderNIO();
        headers = new ArrayList<Header>();
        pending = new LinkedList<DaapRequest>();
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public DaapRequest read() throws IOException {

        DaapRequest ret = null;

        if (pending.isEmpty() == false)
            ret = pending.removeFirst();

        String line = null;

        while ((line = lineReader.read(in, connection.getReadChannel())) != null) {

            bytesRead += in.position();

            if (requestLine == null) {
                requestLine = line;

            } else {
                int p = line.indexOf(':');

                if (p == -1) {
                    requestLine = null;
                    headers.clear();
                    lineReader = null;
                    throw new IOException("Malformed Header");
                }

                String name = line.substring(0, p).trim();
                String value = line.substring(++p).trim();
                headers.add(new BasicHeader(name, value));
            }
        }

        if (lineReader.isComplete()) {

            DaapRequest request = null;

            try {
                request = new DaapRequest(connection, requestLine);
                request.addHeaders(headers);
            } catch (URISyntaxException e) {
                IOException ioe = new IOException();
                ioe.initCause(e);
                throw ioe;
            } finally {
                requestLine = null;
                headers.clear();
            }

            if (ret == null) {
                ret = request;
            } else {
                pending.addLast(request);
            }
        } else if (headers.size() >= 64) {
            requestLine = null;
            headers.clear();
            throw new IOException("Header too large");
        }

        return ret;
    }
}
