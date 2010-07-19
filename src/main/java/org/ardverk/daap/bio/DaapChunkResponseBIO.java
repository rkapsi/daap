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

import java.io.IOException;
import java.io.OutputStream;

import org.ardverk.daap.DaapChunkResponse;
import org.ardverk.daap.DaapRequest;

/**
 * A Chunk Response.
 * 
 * @author Roger Kapsi
 */
public class DaapChunkResponseBIO extends DaapChunkResponse {

    private boolean headerWritten = false;
    private boolean dataWritten = false;

    private OutputStream out;

    /** Creates a new instance of DaapChunkResponse */
    public DaapChunkResponseBIO(DaapRequest request, byte[] data) {
        super(request, data);

        DaapConnectionBIO connection = (DaapConnectionBIO) request
                .getConnection();
        out = connection.getOutputStream();
    }

    public boolean hasRemaining() {
        return !(headerWritten && dataWritten);
    }

    /**
     * 
     * @throws IOException
     * @return
     */
    public boolean write() throws IOException {

        if (!headerWritten) {
            out.write(header, 0, header.length);
            out.flush();

            headerWritten = true;
        }

        if (!dataWritten) {

            out.write(data, 0, data.length);
            out.flush();

            dataWritten = true;
        }

        return true;
    }
}
