
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ShortChunk;

/**
 * The number of the disc where this song is.
 * You can maybe map this to an IDv2/IDv3 Tag.
 */
public class SongDiscNumber extends ShortChunk {
	
	/**
	 * Creates a new SongDiscNumber where disc is 0.
	 * You can change this value with {@see #setValue(int)}.
	 */
	public SongDiscNumber() {
		this(0);
	}
	
	/**
	 * Creates a new SongDiscNumber with the assigned disc.
	 * You can change this value with {@see #setValue(int)}.
	 * @param <tt>disc</tt> the disc of this song.
	 */
	public SongDiscNumber(int disc) {
		super("asdn", "daap.songdiscnumber", disc);
	}
}
