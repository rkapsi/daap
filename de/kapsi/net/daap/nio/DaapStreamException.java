/*
 * DaapStreamException.java
 *
 * Created on April 9, 2004, 1:01 PM
 */

package de.kapsi.net.daap.nio;

import java.io.IOException;

/**
 * iTunes closes audio streams abruptly when the user
 * presses the pause button, on fast-forward and so on.
 * NIO throws in that case an IOException which is not
 * very helpful to differ real IOExceptions from these
 * daily exceptions. 
 */
public class DaapStreamException extends IOException {
    
    public DaapStreamException(IOException err) {
        super(err.getMessage());
    }
}
