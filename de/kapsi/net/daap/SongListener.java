
package de.kapsi.net.daap;

/**
 *
 */
interface SongListener {
    
    static final int SONG_CHANGED	= 1;
    static final int SONG_ADDED		= 2;
    static final int SONG_DELETED	= 3;
    
    public void songEvent(Song song, int event);
}
