
package de.kapsi.net.daap;

import java.io.IOException;

/**
 * iTunes closes audio streams abruptly when the user
 * presses the pause button, on fast-forward and so on.
 * This Exception is thrown whenever a SocketException (BIO)
 * or IOException (NIO) occurs in an audio stream.
 */
public class DaapStreamException extends IOException {
    
    public DaapStreamException(IOException err) {
        super(err.getMessage());
    }
}
