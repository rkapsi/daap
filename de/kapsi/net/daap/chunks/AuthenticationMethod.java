
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ByteChunk;

/**
 * Unknown purpose.
 */
public class AuthenticationMethod extends ByteChunk {
    
    public AuthenticationMethod() {
        this(0);
    }
    
    public AuthenticationMethod(int method) {
        super("msau", "dmap.authenticationmethod", method);
    }
}
