
package de.kapsi.net.daap;

import de.kapsi.net.daap.chunks.*;
import java.io.OutputStream;
import java.io.IOException;

public class Update extends UpdateResponse {

	private final Status status = new Status(200);
	private final ServerRevision serverRevision = new ServerRevision(1);
	
	public Update(int rev) { //Database database) {
		super();
		
		serverRevision.setValue(rev);//database.getRevisionNumber());
		
		add(status);
		add(serverRevision);
		
		/*DeletedSongs deletedSongs = database.getDeletedSongs();
		if (deletedSongs.size() != 0) {
			add(deletedSongs);
		}*/
	}
}
