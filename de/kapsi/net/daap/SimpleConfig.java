package de.kapsi.net.daap;

import java.net.InetAddress;

/**
 * A simple implementation of DaapConfig
 */
public class SimpleConfig implements DaapConfig {
    
    private String serverName;
    
    private InetAddress bindAddr;
    private int port;
    private int backlog;
    
    private int maxConnections;
    
    public SimpleConfig() {
        this(DEFAULT_SERVER_NAME, DEFAULT_PORT, DEFAULT_BACKLOG, null);
    }

    public SimpleConfig(String serverName) {
        this(serverName, DEFAULT_PORT, DEFAULT_BACKLOG, null);
    }
    
    public SimpleConfig(int port) {
        this(DEFAULT_SERVER_NAME, port, DEFAULT_BACKLOG, null);
    }
    
    public SimpleConfig(String serverName, int port) {
        this(serverName, port, DEFAULT_BACKLOG, null);
    }
    
    public SimpleConfig(String serverName, int port, int backlog) {
        this(serverName, port, backlog, null);
    }
    
    public SimpleConfig(String serverName, int port, int backlog, InetAddress bindAddr) {
        this.serverName = serverName;
        this.port = port;
        this.backlog = backlog;
        this.bindAddr = bindAddr;
        
        this.maxConnections = DEFAULT_MAX_CONNECTIONS;
    }
    
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public int getPort() {
		return port;
	}
	
    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }
    
    public int getBacklog() {
        return backlog;
    }
    
    public void setBindAddress(InetAddress bindAddr) {
        this.bindAddr = bindAddr;
    }
    
    public InetAddress getBindAddress() {
        return bindAddr;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    
    public int getMaxConnections() {
        return maxConnections;
    }
}
