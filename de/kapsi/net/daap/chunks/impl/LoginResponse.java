
package de.kapsi.net.daap.chunks.impl;

import de.kapsi.net.daap.chunks.ContainerChunk;

/**
 * Container for the <tt>/login</tt> request.
 */
public class LoginResponse extends ContainerChunk {
    
    public LoginResponse() {
        super("mlog", "dmap.loginresponse");
    }
}
