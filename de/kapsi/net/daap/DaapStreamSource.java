
package de.kapsi.net.daap;

import java.io.IOException;
import java.io.InputStream;

/**
 * Use this interface to implement a Stream Source
 */
public interface DaapStreamSource {
    
    /**
     * Returns an <tt>InputStream</tt> for the provided
     * Song.
     */
    public InputStream getSource(Song song) throws IOException;
}
