
package de.kapsi.net.daap;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public interface DaapStreamSource {
	
    /**
     * 
     */
    public InputStream getSource(Song song) throws IOException;
}
