
package de.kapsi.net.daap.chunks;

import de.kapsi.net.daap.chunks.impl.Status;
import de.kapsi.net.daap.chunks.impl.ServerRevision;
import de.kapsi.net.daap.chunks.impl.UpdateResponse;

/**
 * This class implements the UpdateResponse
 */
public final class UpdateResponseImpl extends UpdateResponse {
    
    public UpdateResponseImpl(int revision) {
        super();
        
        add(new Status(200));
        add(new ServerRevision(revision));
    }
}
