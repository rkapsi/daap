
package de.kapsi.net.daap;

public interface DaapConfig {
    
    public int getPort();
    public int getBacklog();
    
    public int getMaxConnections();
    public String getServerName();
}
