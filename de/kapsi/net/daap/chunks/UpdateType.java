
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class UpdateType extends BooleanChunk {
	
	public UpdateType() {
		this(false);
	}
	
	public UpdateType(boolean updatetype) {
		super("muty", "dmap.updatetype", updatetype);
	}
}
