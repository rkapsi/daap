
package de.kapsi.net.daap;

import de.kapsi.net.daap.chunks.Status;
import de.kapsi.net.daap.chunks.ServerRevision;
import de.kapsi.net.daap.chunks.UpdateResponse;

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
