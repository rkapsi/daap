
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ShortChunk;

/**
 * The bitrate of this song in kilo bits per second (kbps).
 * You can maybe map this to an IDv2/IDv3 Tag.
 */
public class SongBitrate extends ShortChunk {
    
    /**
     * Creates a new SongBitrate with 0 kbps
     * You can change this value with {@see #setValue(int)}.
     */
    public SongBitrate() {
        this(0);
    }
    
    /**
     * Creates a new SongBitrate with the assigned bit rate.
     * You can change this value with {@see #setValue(int)}.
     * @param <tt>bitrate</tt> the bitrate of this song in kbps.
     */
    public SongBitrate(int bitrate) {
        super("asbr", "daap.songbitrate", bitrate);
    }
}
