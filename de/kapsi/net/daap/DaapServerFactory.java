/* 
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004 Roger Kapsi, info at kapsi dot de
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.kapsi.net.daap;

import de.kapsi.net.daap.bio.DaapServerBIO;
import de.kapsi.net.daap.nio.DaapServerNIO;

/**
 * This is a helper class to simplify the DaapServer 
 * creation process
 *
 * @author  Roger Kapsi
 */
public class DaapServerFactory {
    
    /** Creates a new instance of DaapServerFactory */
    private DaapServerFactory() {
    }
    
    /**
     * Creates either a blocking server with Threads or a NIO server
     * 
     * @param library an instance of Library
     * @param nio if true the returned server will be an instance of 
     *          {@see de.kapsi.net.daap.nio.DaapServerNIO} and 
     *          {@see de.kapsi.net.daap.bio.DaapServerBIO} otherwise.
     * @return either {@see de.kapsi.net.daap.nio.DaapServerNIO} or 
     *                  {@see de.kapsi.net.daap.bio.DaapServerBIO}
     */    
    public static DaapServer createServer(Library library, boolean nio) {
        if (nio) {
            return createNIOServer(library);
        } else {
            return createBIOServer(library);
        }
    }
    
    /**
     * Creates either a blocking server with Threads or a NIO server
     * 
     * @param library an instance of Library
     * @param port the Port for the DAAP server
     * @param nio if true the returned server will be an instance of 
     *          {@see de.kapsi.net.daap.nio.DaapServerNIO} and 
     *          {@see de.kapsi.net.daap.bio.DaapServerBIO} otherwise.
     * @return either {@see de.kapsi.net.daap.nio.DaapServerNIO} or 
     *                  {@see de.kapsi.net.daap.bio.DaapServerBIO}
     */    
    public static DaapServer createServer(Library library, int port, boolean nio) {
        if (nio) {
            return createNIOServer(library, port);
        } else {
            return createBIOServer(library, port);
        }
    }
    
    /**
     * Creates either a blocking server with Threads or a NIO server
     * 
     * @param library an instance of Library
     * @param config an instance of DaapConfig
     * @param nio if true the returned server will be an instance of 
     *          {@see de.kapsi.net.daap.nio.DaapServerNIO} and 
     *          {@see de.kapsi.net.daap.bio.DaapServerBIO} otherwise.
     * @return either {@see de.kapsi.net.daap.nio.DaapServerNIO} or 
     *                  {@see de.kapsi.net.daap.bio.DaapServerBIO}
     */    
    public static DaapServer createServer(Library library, DaapConfig config, boolean nio) {
        if (nio) {
            return createNIOServer(library, config);
        } else {
            return createBIOServer(library, config);
        }
    }
    
    /**
     * Creates a blocking server with Threads
     * 
     * @param library an instance of Library
     * @return {@see de.kapsi.net.daap.bio.DaapServerBIO} 
     */    
    public static DaapServer createBIOServer(Library library) {
        return new DaapServerBIO(library);
    }
    
    /**
     * Creates either a blocking server with Threads
     * 
     * @param library an instance of Library
     * @param port the Port of the DAAP server
     * @return {@see de.kapsi.net.daap.bio.DaapServerBIO} 
     */    
    public static DaapServer createBIOServer(Library library, int port) {
        return new DaapServerBIO(library, port);
    }
    
    /**
     * Creates either a blocking server with Threads
     * 
     * @param library an instance of Library
     * @param config an instance of DaapConfig
     * @return {@see de.kapsi.net.daap.bio.DaapServerBIO} 
     */    
    public static DaapServer createBIOServer(Library library, DaapConfig config) {
        return new DaapServerBIO(library, config);
    }
    
    /**
     * Creates a NIO server
     * 
     * @param library an instance of Library
     * @return {@see de.kapsi.net.daap.nio.DaapServerNIO} 
     */    
    public static DaapServer createNIOServer(Library library) {
        return new DaapServerNIO(library);
    }
    
    /**
     * Creates a NIO server
     * 
     * @param library an instance of Library
     * @param port the Port of the DAAP server
     * @return {@see de.kapsi.net.daap.nio.DaapServerNIO}
     */    
    public static DaapServer createNIOServer(Library library, int port) {
        return new DaapServerNIO(library, port);
    }
    
    /**
     * Creates a NIO server
     * 
     * @param library an instance of Library
     * @param config an instance of DaapConfig
     * @return {@see de.kapsi.net.daap.nio.DaapServerNIO}
     */    
    public static DaapServer createNIOServer(Library library, DaapConfig config) {
        return new DaapServerNIO(library, config);
    }
}
