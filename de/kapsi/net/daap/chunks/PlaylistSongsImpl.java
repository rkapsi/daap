package de.kapsi.net.daap.chunks;

import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import java.util.List;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.util.ArrayIterator;

import de.kapsi.net.daap.Song;
import de.kapsi.net.daap.DaapUtil;

import de.kapsi.net.daap.chunks.impl.Status;
import de.kapsi.net.daap.chunks.impl.UpdateType;
import de.kapsi.net.daap.chunks.impl.Listing;
import de.kapsi.net.daap.chunks.impl.ListingItem;
import de.kapsi.net.daap.chunks.impl.DeletedIdListing;
import de.kapsi.net.daap.chunks.impl.SpecifiedTotalCount;
import de.kapsi.net.daap.chunks.impl.ReturnedCount;
import de.kapsi.net.daap.chunks.impl.ItemId;
import de.kapsi.net.daap.chunks.impl.PlaylistSongs;

/**
 * This class implements the PlaylistSongs
 */
public final class PlaylistSongsImpl extends PlaylistSongs {
    
    private static final Log LOG = LogFactory.getLog(PlaylistSongsImpl.class);
    
    private byte[] serialized = null;
    
    public PlaylistSongsImpl(List items, List newItems, List deletedItems, boolean updateType) {
        super();
        
        add(new Status(200));
        add(new UpdateType(updateType));
        
        int secifiedTotalCount = items.size()-deletedItems.size();
        int returnedCount = newItems.size();
        
        add(new SpecifiedTotalCount(secifiedTotalCount));
        add(new ReturnedCount(returnedCount));
        
        Listing listing = new Listing();
        
        Iterator it = ((updateType) ? newItems : items).iterator();
        
        while(it.hasNext()) {
            ListingItem listingItem = new ListingItem();
            Song song = (Song)it.next();
            
            Iterator properties = new ArrayIterator(DaapUtil.PLAYLIST_SONGS_META);
            while(properties.hasNext()) {
                String key = (String)properties.next();
                
                Chunk chunk = song.getProperty(key);
                
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
            
            it = deletedItems.iterator();
            
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
