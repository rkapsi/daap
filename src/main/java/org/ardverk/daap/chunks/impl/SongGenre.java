/* 
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004 Roger Kapsi, info at kapsi dot de
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ardverk.daap.chunks.impl;

import org.ardverk.daap.chunks.StringChunk;

/**
 * The genre of the Song. You can either use the pre-defined
 * genres like ALTERNATIVE, JAZZ etc. or you can use your
 * custom genres.
 *
 * @author  Roger Kapsi
 */
public class SongGenre extends StringChunk {
    
    /** No genre */
    public static final String NONE = null;
    
    /** Alternative */
    public static final String ALTERNATIVE = "Alternative";
    
    /** Blues/R&amp;B */
    public static final String BLUES_RB = "Blues/R&B";
    
    /** Books &amp; Spoken */
    public static final String BOOKS_SPOKEN = "Books & Spoken";
    
    /** Children's Music */
    public static final String CHILDRENS_MUSIC = "Children's Music";
    
    /** Classical */
    public static final String CLASSICAL = "Classical";
    
    /** Country */
    public static final String COUNTRY = "Country";
    
    /** Dance */
    public static final String DANCE = "Dance";
    
    /** Easy Listening */
    public static final String EASY_LISTENING = "Easy Listening";
    
    /** Electronic */
    public static final String ELECTRONIC = "Electronic";
    
    /** Folk */
    public static final String FOLK = "Folk";
    
    /** Hip Hop/Rap */
    public static final String HIP_HOP_RAP = "Hip Hop/Rap";
    
    /** Holiday */
    public static final String HOLIDAY = "Holiday";
    
    /** House */
    public static final String HOUSE = "House";
    
    /** Industrial */
    public static final String INDUSTRIAL = "Industrial";
    
    /** Jazz */
    public static final String JAZZ = "Jazz";
    
    /** New Age */
    public static final String NEW_AGE = "New Age";
    
    /** Pop */
    public static final String POP = "Pop";
    
    /** Religious */
    public static final String RELIGIOUS = "Religious";
    
    /** Rock */
    public static final String ROCK = "Rock";
    
    /** Soundtrack */
    public static final String SOUNDTRACK = "Soundtrack";
    
    /** Techno */
    public static final String TECHNO = "Techno";
    
    /** Trance */
    public static final String TRANCE = "Trance";
    
    /** Unclassifiable */
    public static final String UNCLASSIFIABLE = "Unclassifiable";
    
    /** World */
    public static final String WORLD = "World";

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