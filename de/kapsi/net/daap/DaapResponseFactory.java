/*
 * DaapResponseFactory.java
 *
 * Created on April 5, 2004, 6:40 PM
 */

package de.kapsi.net.daap;

import java.io.IOException;
import java.io.FileInputStream;

/**
 *
 * @author  roger
 */
public interface DaapResponseFactory {
    
    /**
     *
     * @param connection
     * @return
     */    
    public DaapResponse createAuthResponse();
    
    /**
     *
     * @param connection
     * @param data
     * @return
     */    
    public DaapResponse createChunkResponse(byte[] data);
    
    /**
     *
     * @return
     * @param end
     * @param in
     * @param connection
     * @param pos
     * @throws IOException
     */    
    public DaapResponse createAudioResponse(Song song, FileInputStream in, int pos, int end) throws IOException;
}
