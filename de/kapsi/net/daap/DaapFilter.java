
package de.kapsi.net.daap;

import java.net.InetAddress;

/**
 * This interface enables you to accept ot refuse incoming
 * connection from certaint machines.
 */
public interface DaapFilter {
    public boolean accept(InetAddress address);
}
