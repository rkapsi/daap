/*
 * DaapConnection.java
 *
 * Created on April 5, 2004, 6:48 PM
 */

package de.kapsi.net.daap;

/**
 *
 * @author  roger
 */
public interface DaapConnection {
    
    public DaapSession getSession(boolean create);
    public DaapServer getServer();
}
