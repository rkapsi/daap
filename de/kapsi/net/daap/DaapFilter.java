
package de.kapsi.net.daap;

import java.net.InetAddress;

public interface DaapFilter {
    public boolean accept(InetAddress address);
}
