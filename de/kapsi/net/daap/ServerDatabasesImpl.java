package de.kapsi.net.daap;

import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import java.util.List;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.chunks.Status;
import de.kapsi.net.daap.chunks.UpdateType;
import de.kapsi.net.daap.chunks.Listing;
import de.kapsi.net.daap.chunks.ListingItem;
import de.kapsi.net.daap.chunks.DeletedIdListing;
import de.kapsi.net.daap.chunks.SpecifiedTotalCount;
import de.kapsi.net.daap.chunks.ReturnedCount;
import de.kapsi.net.daap.chunks.ItemId;
import de.kapsi.net.daap.chunks.PersistentId;
import de.kapsi.net.daap.chunks.ItemName;
import de.kapsi.net.daap.chunks.ItemCount;
import de.kapsi.net.daap.chunks.ContainerCount;
import de.kapsi.net.daap.chunks.ServerDatabases;

public final class ServerDatabasesImpl extends ServerDatabases {

    private static final Log LOG = LogFactory.getLog(ServerDatabasesImpl.class);
    
    private byte[] serialized = null;
    
    public ServerDatabasesImpl(List databases, boolean updateType) {
        super();
        
        add(new Status(200));
		add(new UpdateType(updateType));
		
		add(new SpecifiedTotalCount(databases.size()));
		add(new ReturnedCount(databases.size()));
		
		Listing listing = new Listing();
		
		Iterator it = databases.iterator();
		while(it.hasNext()) {
			ListingItem listingItem = new ListingItem();
			
            Database database = (Database)it.next();
			
			listingItem.add(new ItemId(database.getId()));
			listingItem.add(new PersistentId(database.getPersistentId()));
			listingItem.add(new ItemName(database.getName()));
			
            Playlist playlist = database.getMasterPlaylist();
            int itemCount = ((updateType) ? playlist.getNewSongs() : playlist.getSongs()).size();
            int containerCount = database.getPlaylists().size();
            
			listingItem.add(new ItemCount(itemCount));
			listingItem.add(new ContainerCount(containerCount));
			
			listing.add(listingItem);
		}
		
		add(listing);
    }
    
    public void serialize(OutputStream os) throws IOException {
        if (serialized == null) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            super.serialize(buffer);
            buffer.close();
            serialized = buffer.toByteArray();
        }
        
        os.write(serialized, 0, serialized.length);
    }
}
