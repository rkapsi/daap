
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ByteChunk;

/**
 * Purpose unknown. The kind is always <tt>2</tt>
 */
public class SongDataKind extends ByteChunk {
	
	public static final int DEFAULT = 2;
	
	public SongDataKind() {
		this(DEFAULT);
	}
	
	public SongDataKind(int kind) {
		super("asdk", "daap.songdatakind", kind);
	}
}
