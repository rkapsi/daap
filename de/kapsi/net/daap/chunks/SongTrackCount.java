
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ShortChunk;

/**
 * The count of tracks the album has where this song
 * belongs to. You can maybe map this to an IDv2/IDv3 Tag.
 */
public class SongTrackCount extends ShortChunk {
	
	/**
	 * Creates a new SongTrackCount with 0 tracks.
	 * You can change this value with {@see #setValue(int)}.
	 */
	public SongTrackCount() {
		this(0);
	}
	
	/**
	 * Creates a new SongTrackCount with the assigned count.
	 * You can change this value with {@see #setValue(int)}.
	 * @param <tt>count</tt> the count of tracks the album has 
	 * where this song belongs to.
	 */
	public SongTrackCount(int count) {
		super("astc", "daap.songtrackcount", count);
	}
}
