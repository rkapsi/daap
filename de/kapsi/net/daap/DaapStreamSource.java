
package de.kapsi.net.daap;

import java.io.IOException;
import java.io.FileInputStream;

/**
 * Use this interface to implement a Stream Source
 */
public interface DaapStreamSource {
    
    /**
     * Returns an <tt>InputStream</tt> for the provided
     * Song.
     */
    public FileInputStream getSource(Song song) throws IOException;
}
