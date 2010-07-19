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

import org.ardverk.daap.DaapAuthResponse;
import org.ardverk.daap.DaapRequest;

/**
 * DaapAuthResponse.
 * 
 * @author Roger Kapsi
 */
public class DaapAuthResponseNIO extends DaapAuthResponse {

    private ByteBuffer headerBuffer;
    private DaapConnectionNIO connection;

    /** Creates a new instance of DaapAuthResponse */
    public DaapAuthResponseNIO(DaapRequest request) {
        super(request);

        this.connection = (DaapConnectionNIO) request.getConnection();
        headerBuffer = ByteBuffer.wrap(header);
    }

    public boolean hasRemaining() {
        return headerBuffer.hasRemaining();
    }

    public boolean write() throws IOException {
        if (hasRemaining()) {
            connection.getWriteChannel().write(headerBuffer);
            return !hasRemaining();
        }

        return true;
    }
}
