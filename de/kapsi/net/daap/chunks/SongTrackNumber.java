
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ShortChunk;

/**
 * The number of this track. You can maybe map this to an IDv2/IDv3 Tag.
 */
public class SongTrackNumber extends ShortChunk {
	
	/**
	 * Creates a new SongTrackNumber with 0 as the track number.
	 * You can change this value with {@see #setValue(int)}.
	 */
	public SongTrackNumber() {
		this(0);
	}
	
	/**
	 * Creates a new SongTrackNumber with the assigned track number.
	 * You can change this value with {@see #setValue(int)}.
	 * @param <tt>track</tt> the track number
	 */
	public SongTrackNumber(int track) {
		super("astn", "daap.songtracknumber", track);
	}
}
