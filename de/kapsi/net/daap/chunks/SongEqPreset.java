
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SongEqPreset extends StringChunk {
	
	public SongEqPreset() {
		this(null);
	}
	
	public SongEqPreset(String preset) {
		super("aseq", "daap.songeqpreset", preset);
	}
}
