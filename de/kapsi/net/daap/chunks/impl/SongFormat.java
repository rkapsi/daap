
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.StringChunk;

/**
 * The format of this song. Note: the provided list of supported
 * format fields is incomplete.
 */
public class SongFormat extends StringChunk {
    
    public static final String UNKNOWN = null;
    
    /**
     * Audio Interchange File Format (AIFF). This format is very popular
     * upon Apple platforms, and is widely used in professional
     * programs that process digital audio waveforms.
     */
    public static final String AIFF = "aiff";
    
    /**
     * MPEG4 Advanced Audio Coding (AAC).
     */
    public static final String M4A = "m4a";
    
    /**
     * MPEG Audio Layer 3 (MP3)
     */
    public static final String MP3 = "mp3";
    
    /**
     * Wave file (WAV)
     */
    public static final String WAV = "wav";
    
    
    /**
     * Playlist
     */
    public static final String PLS = "pls";
    
    /**
     * Creates a new SongFormat where format is not set.
     * You can change this value with {@see #setValue(String)}.
     */
    public SongFormat() {
        this(UNKNOWN);
    }
    
    /**
     * Creates a new SongFormat with the assigned format.
     * You can change this value with {@see #setValue(String)}.
     * @param <tt>format</tt> the format of this song or <tt>null</tt>
     * if no format is set.
     */
    public SongFormat(String format) {
        super("asfm", "daap.songformat", format);
    }
}
