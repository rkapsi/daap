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
    public SimpleConfig(String serverName, InetSocketAddress bindAddr, int backlog) {
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
    public void setInetSocketAddress(InetSocketAddress bindAddr) {
        this.bindAddr = bindAddr;
    }
    
    /**
     *
     * @return
     */    
    public InetSocketAddress getInetSocketAddress() {
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
