
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.IntChunk;

/**
 * Unknown purpose
 */
public class NormVolume extends IntChunk {
    
    public NormVolume() {
        this(0);
    }
    
    public NormVolume(int volume) {
        super("aeNV", "com.apple.itunes.norm-volume", volume);
    }
}
