
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.IntChunk;

/**
 * Used for the <tt>/update</tt> request and represents
 * the current revisionNumber of the Library.
 */
public class ServerRevision extends IntChunk {
	
	public ServerRevision() {
		this(0);
	}
	
	public ServerRevision(int count) {
		super("musr", "dmap.serverrevision", count);
	}
}
