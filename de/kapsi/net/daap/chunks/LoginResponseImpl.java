
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.chunks.impl.Status;
import de.kapsi.net.daap.chunks.impl.SessionId;
import de.kapsi.net.daap.chunks.impl.LoginResponse;

/**
 * This class implements the LoginResponse
 */
public final class LoginResponseImpl extends LoginResponse {
    
    public LoginResponseImpl(int sessionId) {
        super();
        
        add(new Status(200));
        add(new SessionId(sessionId));
    }
}
