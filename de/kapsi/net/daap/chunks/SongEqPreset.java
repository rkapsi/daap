
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.StringChunk;

/**
 * The selected equalizer for this song.
 */
public class SongEqPreset extends StringChunk {
	
	public static final String NONE				= null;
	public static final String ACOUSTIC			= "Acoustic";
	public static final String BASS_BOOSTER		= "Bass Booster";
	public static final String BASS_REDUCER		= "Bass Reducer";
	public static final String CLASSICAL		= "Classical";
	public static final String DANCE			= "Dance";
	public static final String DEEP				= "Deep";
	public static final String ELECTRONIC		= "Electronic";
	public static final String FLAT				= "Flat";
	public static final String HIP_HOP			= "Hip-Hop";
	public static final String JAZZ				= "Jazz";
	public static final String LATIN			= "Latin";
	public static final String LOUDNESS			= "Loudness";
	public static final String LOUNGE			= "Lounge";
	public static final String PIANO			= "Piano";
	public static final String POP				= "Pop";
	public static final String RB				= "R&B";
	public static final String ROCK				= "Rock";
	public static final String SMALL_SPEAKERS   = "Small Speakers";
	public static final String SPOKEN_WORD		= "Spoken Word";
	public static final String TREBLE_BOOSTER   = "Treble Booster";
	public static final String TREBLE_REDUCER   = "Treble Reducer";
	public static final String VOCAL_BOOSTER	= "Vocal Booster";
	
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
