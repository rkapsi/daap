
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ByteChunk;

/**
 * You can use this class to assign a rating to a song to indicate how much
 * you like it. iTunes displays this rating as a set of stars.
 */
public class SongUserRating extends ByteChunk {
    
    /**
     * Constant field for zero stars.
     */
    public static final int NONE	= 0;
    
    /**
     * Constant field for one star.
     */
    public static final int ONE		= 20;
    
    /**
     * Constant field for two stars.
     */
    public static final int TWO		= 40;
    
    /**
     * Constant field for three stars.
     */
    public static final int THREE   = 60;
    
    /**
     * Constant field for four stars.
     */
    public static final int FOUR	= 80;
    
    /**
     * Constant field for five stars.
     */
    public static final int FIVE	= 100;
    
    /**
     * Creates a new SongUserRating with zero stars.
     * Use {@see #setValue(int)} change this value.
     */
    public SongUserRating() {
        this(NONE);
    }
    
    /**
     * Creates a new SongUserRating with the assigned rating.
     * You can change this value with {@see #setValue(int)}.
     * @param <tt>rating</tt> the rating
     */
    public SongUserRating(int rating) {
        super("asur", "daap.songuserrating", rating);
    }
}
