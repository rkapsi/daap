
package de.kapsi.net.daap;

import java.net.SocketAddress;

/**
 * Interface for DaapServer Settings
 */
public interface DaapConfig {
    
    /**
     * Returns the HTTP style name of the Server (e.g. DaapServer/0.1)
     * @return
     */    
    public String getServerName();
    
    /**
     *
     * @return
     */    
    public SocketAddress getSocketAddress();
    
    /**
     *
     * @return
     */    
    public int getBacklog();
    
    /**
     *
     * @return
     */    
    public int getMaxConnections();
}
