
package de.kapsi.net.daap;

import java.net.InetAddress;

/**
 * This interface enables you to accept or refuse incoming
 * connection from certaint machines.
 */
public interface DaapFilter {
       
    /**
     * Return <tt>true</tt> if incoming connection
     * from <tt>address</tt> is allowed to connect
     */
    public boolean accept(InetAddress address);
}
