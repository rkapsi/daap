
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.StringChunk;

/**
 * The genre of this song. You can maybe map this to an IDv2/IDv3 Tag.
 */
public class SongGenre extends StringChunk {
	
	public static final String NONE				= null;
	
	public static final String ALTERNATIVE		= "Alternative";
	public static final String BLUES_RB			= "Blues/R&B";
	public static final String BOOKS_SPOKEN		= "Books & Spoken";
	public static final String CHILDRENS_MUSIC  = "Children’s Music";
	public static final String CLASSICAL		= "Classical";
	public static final String COUNTRY			= "Country";
	public static final String DANCE			= "Dance";
	public static final String EASY_LISTENING   = "Easy Listening";
	public static final String ELECTRONIC		= "Electronic";
	public static final String FOLK				= "Folk";
	public static final String HIP_HOP_RAP		= "Hip Hop/Rap";
	public static final String HOLIDAY			= "Holiday";
	public static final String HOUSE			= "House";
	public static final String INDUSTRIAL		= "Industrial";
	public static final String JAZZ				= "Jazz";
	public static final String NEW_AGE			= "New Age";
	public static final String POP				= "Pop";
	public static final String RELIGIOUS		= "Religious";
	public static final String ROCK				= "Rock";
	public static final String SOUNDTRACK		= "Soundtrack";
	public static final String TECHNO			= "Techno";
	public static final String TRANCE			= "Trance";
	public static final String UNCLASSIFIABLE   = "Unclassifiable";
	public static final String WORLD			= "World";
	
	/**
	 * Creates a new SongGenre where genre is not set.
	 * You can change this value with {@see #setValue(String)}.
	 */
	public SongGenre() {
		this(NONE);
	}
	
	/**
	 * Creates a new SongGenre with the assigned genre.
	 * You can change this value with {@see #setValue(String)}.
	 * @param <tt>genre</tt> the genre of this song or <tt>null</tt>
	 * if no genre is set.
	 */
	public SongGenre(String genre) {
		super("asgn", "daap.songgenre", genre);
	}
}
