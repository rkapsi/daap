package de.kapsi.net.daap;

import java.net.SocketAddress;
import java.net.InetSocketAddress;

/**
 * A simple implementation of DaapConfig
 */
public class SimpleConfig implements DaapConfig {
    
    /**
     * The "HTTP" style name of the Server. Original is "iTunes/4.2 (Mac OS X)"
     */
    public static final String DEFAULT_SERVER_NAME 
            = "DaapServer/0.1 (" + System.getProperty("os.name") + ")";
    
    /**
     * Default iTunes Port is 3689
     */
    public static final int DEFAULT_PORT = 3689;
    
    /**
     * See ServerSocket/SocketChannel description
     */
    public static final int DEFAULT_BACKLOG = 0;
    
    /**
     * The maximum number of connections (you must multipy 
     * this value with 2 as streams are separate connections)
     */
    public static final int DEFAULT_MAX_CONNECTIONS = 5;
    
    private String serverName;
    
    private SocketAddress bindAddr;
    private int backlog;
    
    private int maxConnections;
    
    public SimpleConfig() {
        this(DEFAULT_SERVER_NAME, new InetSocketAddress(DEFAULT_PORT), DEFAULT_BACKLOG);
    }
    
    /**
     *
     * @param serverName
     */    
    public SimpleConfig(String serverName) {
        this(serverName, new InetSocketAddress(DEFAULT_PORT), DEFAULT_BACKLOG);
    }
    
    /**
     *
     * @param port
     */    
    public SimpleConfig(int port) {
        this(DEFAULT_SERVER_NAME, new InetSocketAddress(port), DEFAULT_BACKLOG);
    }
    
    /**
     *
     * @param serverName
     * @param port
     */    
    public SimpleConfig(String serverName, int port) {
        this(serverName, new InetSocketAddress(port), DEFAULT_BACKLOG);
    }
    
    /**
     *
     * @param serverName
     * @param port
     * @param backlog
     */    
    public SimpleConfig(String serverName, int port, int backlog) {
        this(serverName, new InetSocketAddress(port), backlog);
    }
    
    /**
     *
     * @param serverName
     * @param port
     * @param backlog
     * @param bindAddr
     */    
    public SimpleConfig(String serverName, SocketAddress bindAddr, int backlog) {
        this.serverName = serverName;
        this.bindAddr = bindAddr;
        this.backlog = backlog;
        
        this.maxConnections = DEFAULT_MAX_CONNECTIONS;
    }
    
    /**
     *
     * @param serverName
     */    
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    /**
     *
     * @return
     */    
    public String getServerName() {
        return serverName;
    }
    
    /**
     *
     * @param backlog
     */    
    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }
    
    /**
     *
     * @return
     */    
    public int getBacklog() {
        return backlog;
    }
    
    /**
     *
     * @param bindAddr
     */    
    public void setSocketAddress(SocketAddress bindAddr) {
        this.bindAddr = bindAddr;
    }
    
    /**
     *
     * @return
     */    
    public SocketAddress getSocketAddress() {
        return bindAddr;
    }
    
    /**
     *
     * @param maxConnections
     */    
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    
    /**
     *
     * @return
     */    
    public int getMaxConnections() {
        return maxConnections;
    }
}
