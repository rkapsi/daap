package de.kapsi.net.daap;

import java.net.InetAddress;

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
    
    private InetAddress bindAddr;
    private int port;
    private int backlog;
    
    private int maxConnections;
    
    public SimpleConfig() {
        this(DEFAULT_SERVER_NAME, DEFAULT_PORT, DEFAULT_BACKLOG, null);
    }
    
    /**
     *
     * @param serverName
     */    
    public SimpleConfig(String serverName) {
        this(serverName, DEFAULT_PORT, DEFAULT_BACKLOG, null);
    }
    
    /**
     *
     * @param port
     */    
    public SimpleConfig(int port) {
        this(DEFAULT_SERVER_NAME, port, DEFAULT_BACKLOG, null);
    }
    
    /**
     *
     * @param serverName
     * @param port
     */    
    public SimpleConfig(String serverName, int port) {
        this(serverName, port, DEFAULT_BACKLOG, null);
    }
    
    /**
     *
     * @param serverName
     * @param port
     * @param backlog
     */    
    public SimpleConfig(String serverName, int port, int backlog) {
        this(serverName, port, backlog, null);
    }
    
    /**
     *
     * @param serverName
     * @param port
     * @param backlog
     * @param bindAddr
     */    
    public SimpleConfig(String serverName, int port, int backlog, InetAddress bindAddr) {
        this.serverName = serverName;
        this.port = port;
        this.backlog = backlog;
        this.bindAddr = bindAddr;
        
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
     * @param port
     */    
    public void setPort(int port) {
        this.port = port;
    }
    
    /**
     *
     * @return
     */    
    public int getPort() {
        return port;
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
    public void setBindAddress(InetAddress bindAddr) {
        this.bindAddr = bindAddr;
    }
    
    /**
     *
     * @return
     */    
    public InetAddress getBindAddress() {
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
