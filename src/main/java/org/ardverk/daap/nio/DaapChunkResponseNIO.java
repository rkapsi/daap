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

import org.ardverk.daap.DaapChunkResponse;
import org.ardverk.daap.DaapRequest;

/**
 * DaapChunkResponse.
 * 
 * @author Roger Kapsi
 */
public class DaapChunkResponseNIO extends DaapChunkResponse {

    private DaapConnectionNIO connection;
    private ByteBuffer headerBuffer;
    private ByteBuffer dataBuffer;

    /** Creates a new instance of DaapChunkResponse */
    public DaapChunkResponseNIO(DaapRequest request, byte[] data) {
        super(request, data);

        this.connection = (DaapConnectionNIO) request.getConnection();
        headerBuffer = ByteBuffer.wrap(header);
        dataBuffer = ByteBuffer.wrap(data);
    }

    public boolean hasRemaining() {
        return headerBuffer.hasRemaining() || dataBuffer.hasRemaining();
    }

    public boolean write() throws IOException {

        if (headerBuffer.hasRemaining()) {
            connection.getWriteChannel().write(headerBuffer);

            if (headerBuffer.hasRemaining())
                return false;

        }

        if (dataBuffer.hasRemaining()) {
            connection.getWriteChannel().write(dataBuffer);
            return !dataBuffer.hasRemaining();
        }

        return true;
    }
}
