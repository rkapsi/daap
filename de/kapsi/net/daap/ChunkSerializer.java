
package de.kapsi.net.daap;

import java.io.OutputStream;
import java.io.IOException;

public interface ChunkSerializer {
	
	public void serialize(OutputStream os) throws IOException;
}
