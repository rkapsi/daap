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

import org.ardverk.daap.chunks.UShortChunk;

/**
* The total number of Tracks.
*
* @author  Roger Kapsi
*/
public class SongTrackCount extends UShortChunk {

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