/*
 * DaapThreadFactory.java
 *
 * Created on April 18, 2004, 12:28 AM
 */

package de.kapsi.net.daap;

/**
 *
 * @author  roger
 */
public interface DaapThreadFactory {
    
    public Thread createDaapThread(Runnable runner, String name);
}
