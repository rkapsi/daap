
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.IntChunk;

/**
 * The stop time of this song in seconds. I.e. you can use it
 * to stop playing this song n-seconds before end.
 */
public class SongStopTime extends IntChunk {
    
    /**
     * Creates a new SongStopTime where stop time is not set.
     * You can change this value with {@see #setValue(int)}.
     */
    public SongStopTime() {
        this(0);
    }
    
    /**
     * Creates a new SongStopTime at the assigned time. Use 0
     * to disable this property.
     * You can change this value with {@see #setValue(int)}.
     * @param <tt>time</tt> the stop time of this song in seconds.
     */
    public SongStopTime(int time) {
        super("assp", "daap.songstoptime", time);
    }
}
