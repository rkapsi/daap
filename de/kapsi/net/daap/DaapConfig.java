
package de.kapsi.net.daap;

import java.net.InetAddress;

/**
 * Interface for DaapServer Settings
 */
public interface DaapConfig {
    
    // "iTunes/4.2 (Mac OS X)"
    public static final String DEFAULT_SERVER_NAME 
            = "DaapServer/0.1 (" + System.getProperty("os.name") + ")";
    
    public static final int DEFAULT_PORT = 3689; // the default iTunes port
    public static final int DEFAULT_BACKLOG = 0; // 
    public static final int DEFAULT_MAX_CONNECTIONS = 4; // like iTunes

    public String getServerName();
    
    public InetAddress getBindAddress();
    public int getPort();
    public int getBacklog();
    
    public int getMaxConnections();
}
