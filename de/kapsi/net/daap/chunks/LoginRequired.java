
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class LoginRequired extends BooleanChunk {
	
	public LoginRequired() {
		this(false);
	}
	
	public LoginRequired(boolean required) {
		super("mslr", "dmap.loginrequired", required);
	}
}
