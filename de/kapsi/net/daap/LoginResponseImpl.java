
package de.kapsi.net.daap;

import de.kapsi.net.daap.chunks.Status;
import de.kapsi.net.daap.chunks.SessionId;
import de.kapsi.net.daap.chunks.LoginResponse;

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
