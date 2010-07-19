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

import org.ardverk.daap.DaapRequest;
import org.ardverk.daap.DaapResponse;
import org.ardverk.daap.DaapResponseFactory;
import org.ardverk.daap.Song;

/**
 * This factory creates BIO DaapRespones.
 * 
 * @author Roger Kapsi
 */
class DaapResponseFactoryBIO implements DaapResponseFactory {

    /** Creates a new instance of DaapResponseFactoryBIO */
    protected DaapResponseFactoryBIO() {
    }

    public DaapResponse createAudioResponse(DaapRequest request, Song song,
            File file, long pos, long end) throws IOException {
        return new DaapAudioResponseBIO(request, song, file, pos, end);
    }

    public DaapResponse createAudioResponse(DaapRequest request, Song song,
            FileInputStream in, long pos, long end) throws IOException {
        return new DaapAudioResponseBIO(request, song, in, pos, end);
    }

    public DaapResponse createAuthResponse(DaapRequest request) {
        return new DaapAuthResponseBIO(request);
    }

    public DaapResponse createChunkResponse(DaapRequest request, byte[] data) {
        return new DaapChunkResponseBIO(request, data);
    }

    public DaapResponse createNoContentResponse(DaapRequest request) {
        return new DaapNoContentResponseBIO(request);
    }
}
