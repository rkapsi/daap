
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.ContainerChunk;
import java.util.ArrayList;

/**
 * Container for the <tt>/login</tt> request.
 */
public class LoginResponse extends ContainerChunk {
    
    public LoginResponse() {
        super("mlog", "dmap.loginresponse");
    }
}
