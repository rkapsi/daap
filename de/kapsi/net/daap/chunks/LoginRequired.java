
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.BooleanChunk;

/**
 * Unknown purpose. Used by {@link de.kapsi.net.daap.ServerInfoResponseImpl ServerInfoResponseImpl}
 * but Auth requests are triggered by HTTP and iTunes doesn't care about
 * this chunk
 */
public class LoginRequired extends BooleanChunk {
	
	public LoginRequired() {
		this(false);
	}
	
	public LoginRequired(boolean required) {
		super("mslr", "dmap.loginrequired", required);
	}
}
