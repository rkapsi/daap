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

import org.ardverk.daap.bio.DaapServerBIO;
import org.ardverk.daap.nio.DaapServerNIO;

/**
 * This is a helper class to simplify the DaapServer creation process
 * 
 * @author Roger Kapsi
 */
public class DaapServerFactory {

    /** Creates a new instance of DaapServerFactory */
    private DaapServerFactory() {
    }

    /**
     * Creates either a blocking server with Threads or a NIO server
     * 
     * @param library
     *            an instance of Library
     * @param nio
     *            if true the returned server will be an instance of {@see
     *            de.kapsi.net.daap.nio.DaapServerNIO} and {@see
     *            de.kapsi.net.daap.bio.DaapServerBIO} otherwise.
     * @return either {@see de.kapsi.net.daap.nio.DaapServerNIO} or {@see
     *         de.kapsi.net.daap.bio.DaapServerBIO}
     */
    public static DaapServer<?> createServer(Library library, boolean nio) {
        if (nio) {
            return createNIOServer(library);
        } else {
            return createBIOServer(library);
        }
    }

    /**
     * Creates either a blocking server with Threads or a NIO server
     * 
     * @param library
     *            an instance of Library
     * @param config
     *            an instance of DaapConfig
     * @param nio
     *            if true the returned server will be an instance of {@see
     *            de.kapsi.net.daap.nio.DaapServerNIO} and {@see
     *            de.kapsi.net.daap.bio.DaapServerBIO} otherwise.
     * @return either {@see de.kapsi.net.daap.nio.DaapServerNIO} or {@see
     *         de.kapsi.net.daap.bio.DaapServerBIO}
     */
    public static DaapServer<?> createServer(Library library,
            DaapConfig config, boolean nio) {
        if (nio) {
            return createNIOServer(library, config);
        } else {
            return createBIOServer(library, config);
        }
    }

    /**
     * Creates a blocking server with Threads
     * 
     * @param library
     *            an instance of Library
     * @return {@see de.kapsi.net.daap.bio.DaapServerBIO}
     */
    public static DaapServer<?> createBIOServer(Library library) {
        return new DaapServerBIO(library);
    }

    /**
     * Creates either a blocking server with Threads
     * 
     * @param library
     *            an instance of Library
     * @param config
     *            an instance of DaapConfig
     * @return {@see de.kapsi.net.daap.bio.DaapServerBIO}
     */
    public static DaapServer<?> createBIOServer(Library library,
            DaapConfig config) {
        return new DaapServerBIO(library, config);
    }

    /**
     * Creates a NIO server
     * 
     * @param library
     *            an instance of Library
     * @return {@see de.kapsi.net.daap.nio.DaapServerNIO}
     */
    public static DaapServer<?> createNIOServer(Library library) {
        return new DaapServerNIO(library);
    }

    /**
     * Creates a NIO server
     * 
     * @param library
     *            an instance of Library
     * @param config
     *            an instance of DaapConfig
     * @return {@see de.kapsi.net.daap.nio.DaapServerNIO}
     */
    public static DaapServer<?> createNIOServer(Library library,
            DaapConfig config) {
        return new DaapServerNIO(library, config);
    }
}
