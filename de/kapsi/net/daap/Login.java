
package de.kapsi.net.daap;

import de.kapsi.net.daap.chunks.*;
import java.util.*;
import java.io.OutputStream;
import java.io.IOException;

public class Login extends LoginResponse {
	
	private final Status status = new Status(200);
	private final SessionId sessionId = new SessionId();
	
	public Login(int sId) {
		super();
		
		sessionId.setValue(sId);
		
		add(status);
		add(sessionId);
	}
}
