
package de.kapsi.net.daap;

import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import java.util.List;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.util.ArrayIterator;

import de.kapsi.net.daap.chunks.Status;
import de.kapsi.net.daap.chunks.UpdateType;
import de.kapsi.net.daap.chunks.Listing;
import de.kapsi.net.daap.chunks.ListingItem;
import de.kapsi.net.daap.chunks.DeletedIdListing;
import de.kapsi.net.daap.chunks.SpecifiedTotalCount;
import de.kapsi.net.daap.chunks.ReturnedCount;
import de.kapsi.net.daap.chunks.ItemId;
import de.kapsi.net.daap.chunks.DatabasePlaylists;

/**
 * This class is an implementation of DatabasePlaylists
 */
public final class DatabasePlaylistsImpl extends DatabasePlaylists {
    
    private static final Log LOG = LogFactory.getLog(DatabasePlaylistsImpl.class);
    
    private byte[] serialized = null;
    
    public DatabasePlaylistsImpl(List containers, List deletedContainers, boolean updateType) {
        super();
        
        add(new Status(200));
        add(new UpdateType(updateType));
        
        int specifiedTotalCount = containers.size()-deletedContainers.size();
        int returnedCount = specifiedTotalCount;
        
        add(new SpecifiedTotalCount(specifiedTotalCount));
        add(new ReturnedCount(returnedCount));
        
        Listing listing = new Listing();
        
        Iterator it = containers.iterator();
        while(it.hasNext()) {
            ListingItem listingItem = new ListingItem();
            Playlist playlist = (Playlist)it.next();
            
            Iterator properties = new ArrayIterator(DaapUtil.DATABASE_PLAYLISTS_META);
            while(properties.hasNext()) {
                String key = (String)properties.next();
                Chunk chunk = playlist.getProperty(key);
                
                if (chunk != null) {
                    listingItem.add(chunk);
                    
                } else if (LOG.isInfoEnabled()) {
                    LOG.info("Unknown chunk type: " + key);
                }
            }
            
            listing.add(listingItem);
        }
        
        add(listing);
        
        if (updateType) {
            
            it = deletedContainers.iterator();
            
            if (it.hasNext()) {
                
                DeletedIdListing deletedListing = new DeletedIdListing();
                
                while(it.hasNext()) {
                    Integer itemId = (Integer)it.next();
                    deletedListing.add(new ItemId(itemId.intValue()));
                }
                
                add(deletedListing);
            }
        }
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
