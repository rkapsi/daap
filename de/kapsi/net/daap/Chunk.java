
package de.kapsi.net.daap;

import java.io.OutputStream;
import java.io.IOException;

public interface Chunk extends ChunkSerializer {

	public int chunkSize();
	public int chunkLength();
	public int chunkTypeCode();
}
