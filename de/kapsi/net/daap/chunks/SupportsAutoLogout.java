
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.*;

public class SupportsAutoLogout extends BooleanChunk {
	
	public SupportsAutoLogout() {
		this(false);
	}
	
	public SupportsAutoLogout(boolean supports) {
		super("msal", "dmap.supportsautologout", supports);
	}
}
