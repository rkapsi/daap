
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.StringChunk;

/**
 * This is the description of the song format and not of the song!
 * For example is the description of a MP3 file 'MPEG audio file'.
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
     * Creates a new SongDescription where no description is set.
     * You can change this value with {@see #setValue(String)}.
     */
    public SongDescription() {
        this(null);
    }
    
    /**
     * Creates a new SongDescription with the assigned description.
     * You can change this value with {@see #setValue(String)}.
     * @param <tt>description</tt> the description of the format of this song.
     */
    public SongDescription(String description) {
        super("asdt", "daap.songdescription", description);
    }
}
