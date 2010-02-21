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

import org.ardverk.daap.chunks.StringChunk;

/**
 * This is the description of the Song format and not of the song! For example
 * is the description of a MP3 file 'MPEG audio file'.
 * 
 * @author Roger Kapsi
 */
public class SongDescription extends StringChunk {

    /**
     * Description for a MPEG Audio Layer 3 file (MP3)
     */
    public static final String MPEG_AUDIO_FILE = "MPEG audio file";

    /**
     * Description for a Audio Interchange File Format file (AIFF)
     */
    public static final String AIFF_AUDIO_FILE = "AIFF audio file";

    /**
     * Description for a MPEG4 Advanced Audio Coding file (AAC)
     */
    public static final String AAC_AUDIO_FILE = "AAC audio file";

    /**
     * Description for a WAV file (WAV)
     */
    public static final String WAV_AUDIO_FILE = "WAV audio file";

    /**
     * Description for a Playlist URL
     */
    public static final String PLAYLIST_URL = "Playlist URL";

    /**
     * Creates a new SongDescription where no description is set. You can change
     * this value with {@see #setValue(String)}.
     */
    public SongDescription() {
        this(null);
    }

    /**
     * Creates a new SongDescription with the assigned description. You can
     * change this value with {@see #setValue(String)}.
     * 
     * @param <tt>description</tt> the description of the format of this song.
     */
    public SongDescription(String description) {
        super("asdt", "daap.songdescription", description);
    }
}