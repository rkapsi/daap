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

import java.net.InetSocketAddress;

/**
 * A simple implementation of DaapConfig
 *
 * @author  Roger Kapsi
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
    
    private InetSocketAddress bindAddr;
    private int backlog;
    
    private int maxConnections;
    
    /** Creates a new SimpleConfig with default settings */
    public SimpleConfig() {
        this(DEFAULT_SERVER_NAME, new InetSocketAddress(DEFAULT_PORT), DEFAULT_BACKLOG);
    }
    
    /**
     * Create a new SimpleConfig with the server name
     * 
     * @param serverName the name of the server
     */    
    public SimpleConfig(String serverName) {
        this(serverName, new InetSocketAddress(DEFAULT_PORT), DEFAULT_BACKLOG);
    }
    
    /**
     * Creates a new SimpleConfig with the port
     * 
     * @param port a valid Port
     */    
    public SimpleConfig(int port) {
        this(DEFAULT_SERVER_NAME, new InetSocketAddress(port), DEFAULT_BACKLOG);
    }
    
    /**
     * Creates a new SimpleConfig with the server name and Port
     * 
     * @param serverName a server name
     * @param port a valid Port
     */    
    public SimpleConfig(String serverName, int port) {
        this(serverName, new InetSocketAddress(port), DEFAULT_BACKLOG);
    }
    
    /**
     * Creates a new SimpleConfig with the server name, Port and backlog
     * 
     * @param serverName a server name
     * @param port a valid Port
     * @param backlog the backlog for the {@see java.net.ServerSocket}
     */    
    public SimpleConfig(String serverName, int port, int backlog) {
        this(serverName, new InetSocketAddress(port), backlog);
    }
    
    /**
     * Creates a new SimpleConfig with the server name, the bind address and backlog
     * 
     * @param serverName a server name
     * @param bindAddr a valid IP/Port
     * @param backlog the backlog for the {@see java.net.ServerSocket}
     */    
    public SimpleConfig(String serverName, InetSocketAddress bindAddr, int backlog) {
        this.serverName = serverName;
        this.bindAddr = bindAddr;
        this.backlog = backlog;
        
        this.maxConnections = DEFAULT_MAX_CONNECTIONS;
    }
    
    /**
     * Sets the server name
     * 
     * @param serverName a server name
     */    
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    /**
     * Returns the server name
     * 
     * @return server name
     */    
    public String getServerName() {
        return serverName;
    }
    
    /**
     * Sets the backlog for the {@see java.net.ServerSocket}
     * 
     * @param backlog a backlog
     */    
    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }
    
    /**
     * Returns the backlog
     * 
     * @return backlog
     */    
    public int getBacklog() {
        return backlog;
    }
    
    /**
     * Sets the bind address
     * 
     * @param bindAddr a valid IP/Port
     */    
    public void setInetSocketAddress(InetSocketAddress bindAddr) {
        this.bindAddr = bindAddr;
    }
    
    /**
     * Returns the bind address
     * 
     * @return bind address
     */    
    public InetSocketAddress getInetSocketAddress() {
        return bindAddr;
    }
    
    /**
     * Sets the maximum number of connections the server
     * can have
     * 
     * @param maxConnections the maximum number of connections
     */    
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    
    /**
     * Returns the maximum number of connections
     * 
     * @return maximum number of connections
     */    
    public int getMaxConnections() {
        return maxConnections;
    }
}
