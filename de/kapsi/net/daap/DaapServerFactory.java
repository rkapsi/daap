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
     *
     * @param library
     * @param nio
     * @return
     */    
    public static DaapServer createServer(Library library, boolean nio) {
        if (nio) {
            return createNIOServer(library);
        } else {
            return createBIOServer(library);
        }
    }
    
    /**
     *
     * @param library
     * @param port
     * @param nio
     * @return
     */    
    public static DaapServer createServer(Library library, int port, boolean nio) {
        if (nio) {
            return createNIOServer(library, port);
        } else {
            return createBIOServer(library, port);
        }
    }
    
    /**
     *
     * @param library
     * @param config
     * @param nio
     * @return
     */    
    public static DaapServer createServer(Library library, DaapConfig config, boolean nio) {
        if (nio) {
            return createNIOServer(library, config);
        } else {
            return createBIOServer(library, config);
        }
    }
    
    /**
     *
     * @param library
     * @return
     */    
    public static DaapServer createBIOServer(Library library) {
        return new DaapServerBIO(library);
    }
    
    /**
     *
     * @param library
     * @param port
     * @return
     */    
    public static DaapServer createBIOServer(Library library, int port) {
        return new DaapServerBIO(library, port);
    }
    
    /**
     *
     * @param library
     * @param config
     * @return
     */    
    public static DaapServer createBIOServer(Library library, DaapConfig config) {
        return new DaapServerBIO(library, config);
    }
    
    /**
     *
     * @param library
     * @return
     */    
    public static DaapServer createNIOServer(Library library) {
        return new DaapServerNIO(library);
    }
    
    /**
     *
     * @param library
     * @param port
     * @return
     */    
    public static DaapServer createNIOServer(Library library, int port) {
        return new DaapServerNIO(library, port);
    }
    
    /**
     *
     * @param library
     * @param config
     * @return
     */    
    public static DaapServer createNIOServer(Library library, DaapConfig config) {
        return new DaapServerNIO(library, config);
    }
}
