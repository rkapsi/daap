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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This Factory interface is used to create either NIO or BIO based
 * DaapResponses.
 * 
 * @author Roger Kapsi
 */
public interface DaapResponseFactory {

    /**
     * 
     * @param request
     * @return
     */
    public DaapResponse createNoContentResponse(DaapRequest request);

    /**
     * Creates an returns a DaapAuthResponse for the passed request.
     * 
     * @param request
     * @return
     */
    public DaapResponse createAuthResponse(DaapRequest request);

    /**
     * Creates and returns a DaapChunResponse for the passed request and data
     * (payload, i.e. the serialized Chunks).
     * 
     * @param request
     * @param data
     * @return
     */
    public DaapResponse createChunkResponse(DaapRequest request, byte[] data);

    /**
     * Creates and returns a DaapAudioResponse for the passed parameters.
     * 
     * @param request
     * @param song
     * @param in
     * @param pos
     * @param end
     * @return
     * @throws IOException
     */
    public DaapResponse createAudioResponse(DaapRequest request, Song song,
            File file, long pos, long end) throws IOException;

    /**
     * Creates and returns a DaapAudioResponse for the passed parameters.
     * 
     * @param request
     * @param song
     * @param in
     * @param pos
     * @param end
     * @return
     * @throws IOException
     */
    public DaapResponse createAudioResponse(DaapRequest request, Song song,
            FileInputStream in, long pos, long end) throws IOException;
}
