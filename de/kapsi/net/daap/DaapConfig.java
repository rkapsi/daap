
package de.kapsi.net.daap;

import java.net.InetAddress;

/**
 * Interface for DaapServer Settings
 */
public interface DaapConfig {
    
    /**
     *
     * @return
     */    
    public String getServerName();
    
    /**
     *
     * @return
     */    
    public InetAddress getBindAddress();
    /**
     *
     * @return
     */    
    public int getPort();
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
