
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.IntChunk;

/**
 * The start time of this song in seconds. I.e. you can use it
 * to skip n-seconds at the beginning.
 */
public class SongStartTime extends IntChunk {
    
    /**
     * Creates a new SongStartTime which starts at the
     * beginning of the song.
     * You can change this value with {@see #setValue(int)}.
     */
    public SongStartTime() {
        this(0);
    }
    
    /**
     * Creates a new SongStartTime at the assigned time.
     * You can change this value with {@see #setValue(int)}.
     * @param <tt>time</tt> the start time of this song in seconds.
     */
    public SongStartTime(int time) {
        super("asst", "daap.songstarttime", time);
    }
}
