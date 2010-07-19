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

package org.ardverk.daap;

/**
 * An abstract base class for DaapChunkRespones. A Chunk is either a single
 * piece of data or a set of Chunks. Derived classes implement the actual
 * DaapChunkResponse.
 * 
 * @author Roger Kapsi
 */
public abstract class DaapChunkResponse implements DaapResponse {

    protected final DaapRequest request;
    protected final byte[] data;
    protected final byte[] header;

    /** Creates a new instance of DaapChunkResponse */
    public DaapChunkResponse(DaapRequest request, byte[] data) {
        this.request = request;
        this.data = data;

        header = DaapHeaderConstructor.createChunkHeader(request, data.length);
    }

    @Override
    public String toString() {
        return (new String(header));
    }
}
