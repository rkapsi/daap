
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.IntChunk;

/**
 * The sample rate of this song in kHz.
 */
public class SongSampleRate extends IntChunk {
    
    /**
     * Creates a new SongSampleRate with 0 kHz
     * You can change this value with {@see #setValue(int)}.
     */
    public SongSampleRate() {
        this(0);
    }
    
    /**
     * Creates a new SongSampleRate with the assigned sample rate.
     * You can change this value with {@see #setValue(int)}.
     * @param <tt>rate</tt> the rate of this song in kHz.
     */
    public SongSampleRate(int rate) {
        super("assr", "daap.songsamplerate", rate);
    }
}
