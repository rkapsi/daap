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

package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.StringChunk;

/**
 * The selected equalizer for this song.
 *
 * @author  Roger Kapsi
 */
public class SongEqPreset extends StringChunk {
    
    public static final String NONE		= null;
    public static final String ACOUSTIC		= "Acoustic";
    public static final String BASS_BOOSTER	= "Bass Booster";
    public static final String BASS_REDUCER	= "Bass Reducer";
    public static final String CLASSICAL	= "Classical";
    public static final String DANCE		= "Dance";
    public static final String DEEP		= "Deep";
    public static final String ELECTRONIC	= "Electronic";
    public static final String FLAT		= "Flat";
    public static final String HIP_HOP		= "Hip-Hop";
    public static final String JAZZ		= "Jazz";
    public static final String LATIN		= "Latin";
    public static final String LOUDNESS		= "Loudness";
    public static final String LOUNGE		= "Lounge";
    public static final String PIANO		= "Piano";
    public static final String POP		= "Pop";
    public static final String RB		= "R&B";
    public static final String ROCK             = "Rock";
    public static final String SMALL_SPEAKERS   = "Small Speakers";
    public static final String SPOKEN_WORD      = "Spoken Word";
    public static final String TREBLE_BOOSTER   = "Treble Booster";
    public static final String TREBLE_REDUCER   = "Treble Reducer";
    public static final String VOCAL_BOOSTER    = "Vocal Booster";
    
    /**
     * Creates a new SongEqPreset where no equalizer is selected.
     * You can change this value with {@see #setValue(String)}.
     */
    public SongEqPreset() {
        this(NONE);
    }
    
    /**
     * Creates a new SongEqPreset with the assigned equalizer.
     * You can change this value with {@see #setValue(String)}.
     * @param <tt>equalizer</tt> the equalizer of this song.
     */
    public SongEqPreset(String preset) {
        super("aseq", "daap.songeqpreset", preset);
    }
}
