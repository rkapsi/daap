/*
 * DaapConnection.java
 *
 * Created on April 5, 2004, 6:48 PM
 */

package de.kapsi.net.daap;

import java.io.IOException;

/**
 *
 * @author  roger
 */
public interface DaapConnection {
    
    public void update() throws IOException;
    public DaapSession getSession(boolean create);
    public DaapServer getServer();
}
