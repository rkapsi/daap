/*
* Digital Audio Access Protocol (DAAP)
* Copyright (C) 2004-2010 Roger Kapsi
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

import org.ardverk.daap.chunks.UByteChunk;

/**
* You can use this class to assign a rating to a Song to indicate
* how much you like or dislike a Song. iTunes displays this rating
* as a set of stars.
*
* @author  Roger Kapsi
*/
public class SongUserRating extends UByteChunk {

/**
* Constant field for zero stars.
*/
public static final int NONE = 0;

/**
* Constant field for one star.
*/
public static final int ONE	= 20;

/**
* Constant field for two stars.
*/
public static final int TWO	= 40;

/**
* Constant field for three stars.
*/
public static final int THREE = 60;

/**
* Constant field for four stars.
*/
public static final int FOUR	 = 80;

/**
* Constant field for five stars.
*/
public static final int FIVE	 = 100;

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
* @param <code>rating</code> the rating
*/
public SongUserRating(int rating) {
super("asur", "daap.songuserrating", rating);
}
}