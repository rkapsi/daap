/*
 * DaapServerFactory.java
 *
 * Created on April 1, 2004, 8:29 PM
 */

package de.kapsi.net.daap;

import de.kapsi.net.daap.bio.DaapServerBIO;
import de.kapsi.net.daap.nio.DaapServerNIO;

/**
 * This is a helper class to simplify the DaapServer 
 * creation process
 *
 * @author  roger
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
