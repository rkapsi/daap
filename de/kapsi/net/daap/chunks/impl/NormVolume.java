
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.IntChunk;

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
