
package de.kapsi.net.daap;

import java.io.IOException;

import de.kapsi.util.ArrayIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.chunks.Chunk;
import de.kapsi.net.daap.chunks.AbstractChunk;
import de.kapsi.net.daap.chunks.DummyChunk;

import de.kapsi.net.daap.chunks.impl.ItemId;
import de.kapsi.net.daap.chunks.impl.ItemName;
import de.kapsi.net.daap.chunks.impl.PersistentId;
import de.kapsi.net.daap.chunks.impl.PlaylistSongs;
import de.kapsi.net.daap.chunks.impl.Status;
import de.kapsi.net.daap.chunks.impl.UpdateType;
import de.kapsi.net.daap.chunks.impl.Listing;
import de.kapsi.net.daap.chunks.impl.ListingItem;
import de.kapsi.net.daap.chunks.impl.DeletedIdListing;
import de.kapsi.net.daap.chunks.impl.SpecifiedTotalCount;
import de.kapsi.net.daap.chunks.impl.ReturnedCount;
import de.kapsi.net.daap.chunks.impl.PlaylistSongs;
import de.kapsi.net.daap.chunks.impl.SmartPlaylist;
import de.kapsi.net.daap.chunks.impl.ItemCount;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The name is self explaining.
 */
public class Playlist implements SongListener {
    
    private static final Log LOG = LogFactory.getLog(Playlist.class);
    
    private static int PLAYLIST_ID = 1;
    
    private static final boolean notifyMasterPlaylistAddSong = true;
    private static final boolean notifyMasterPlaylistRemoveSong = false;
    private static final boolean notifyMasterPlaylistUpdateSong = true;
    
    private static final SmartPlaylist SMART_PLAYLIST = new SmartPlaylist(false);
    private static final DummyChunk DUMMY_CHUNK = new DummyChunk(SMART_PLAYLIST);
    
    private ItemId itemId;
    private ItemName itemName;
    private PersistentId persistentId;
    private ItemCount itemCount;
    
    private ArrayList items;
    private ArrayList newItems;
    private ArrayList deletedItems;
    
    private HashMap properties;
    
    private byte[] playlistSongs;
    private byte[] playlistSongsUpdate;
    
    private Playlist masterPlaylist;
    
    private boolean isSmartPlaylist = false;
    private boolean isclone = true;
    
    public Playlist(String name) {
        isclone = false;
        synchronized(Playlist.class) {
            itemId = new ItemId(PLAYLIST_ID++);
        }
        
        items = new ArrayList();
        newItems = new ArrayList();
        deletedItems = new ArrayList();
        properties = new HashMap();
        
        itemName = new ItemName(name);
        persistentId = new PersistentId(Library.nextPersistentId());
        itemCount = new ItemCount();
        
        add(itemId);
        add(itemName);
        add(persistentId);
        add(DUMMY_CHUNK);
        add(itemCount);
    }
    
    // required for createSnapshot()
    private Playlist(Playlist orig) {
        
        properties = new HashMap();
        
        this.itemId = new ItemId(orig.getId());
        this.itemName = new ItemName(orig.getName());
        
        persistentId = new PersistentId(itemId.getValue());
        itemCount = new ItemCount(orig.itemCount.getValue());
        
        add(itemId);
        add(itemName);
        add(persistentId);
        add(itemCount);
    }
    
    void setMasterPlaylist(Playlist masterPlaylist) {
        this.masterPlaylist = masterPlaylist;
    }
    
    public int getId() {
        return itemId.getValue();
    }
    
    private void add(AbstractChunk chunk) {
        properties.put(chunk.getName(), chunk);
    }
    
    public Chunk getProperty(String property) {
        return (Chunk)properties.get(property);
    }
    
    public void setName(String name) {
        itemName.setValue(name);
    }
    
    public String getName() {
        return itemName.getValue();
    }
    
    public void setSmartPlaylist(boolean smart) {
        isSmartPlaylist = smart;
        
        if (isSmartPlaylist) {
            add(SMART_PLAYLIST);
        } else {
            add(DUMMY_CHUNK);
        }
    }
     
    public boolean isSmartPlaylist() {
        return isSmartPlaylist;
    }
    
    /**
     * Returns a Song for the provided songId or
     * <tt>null</tt> if this id is unknown for
     * this Playlist
     */
    Song getSong(int songId) {
        Iterator it = items.iterator();
        while(it.hasNext()) {
            Song song = (Song)it.next();
            if (song.getId() == songId) {
                return song;
            }
        }
        
        return null;
    }
    
    /**
     * Returns the number of Songs in this Playlist
     */
    public int size() {
        return items.size();
    }
    
    /**
     * Used internally by Database
     */
    List getSongs() {
        return items;
    }
    
    /**
     * Used internally by Database
     */
    List getNewSongs() {
        return newItems;
    }
    
    /**
     * Used internally by Database
     */
    List getDeletedSongs() {
        return deletedItems;
    }
    
    /**
     * Adds <tt>song</tt> to this Playlist
     */
    public void add(Song song) {
        
        if (items.contains(song)==false) {
            
            items.add(song);
            newItems.add(song);
            song.addListener(this);
            
            Integer id = new Integer(song.getContainerId());
            deletedItems.remove(id);
            
            if (notifyMasterPlaylistAddSong && masterPlaylist != null) {
                masterPlaylist.add(song);
            }
            
            itemCount.setValue(items.size());
        }
    }
    
    /**
     * Removes <tt>song</tt> from this Playlist
     * and returns <tt>true</tt>
     */
    public boolean remove(Song song) {
        
        if (items.remove(song)) {
            
            song.removeListener(this);
            newItems.remove(song);
            
            Integer id = new Integer(song.getContainerId());
            deletedItems.add(id);
            
            if (notifyMasterPlaylistRemoveSong && masterPlaylist != null) {
                masterPlaylist.remove(song);
            }
            
            itemCount.setValue(items.size());
            
            return true;
        }
        
        return false;
    }
    
    public void songEvent(Song song, int event) {
        if (event == SongListener.SONG_CHANGED) {
            if (newItems.contains(song)==false) {
                newItems.add(song);
                
                if (notifyMasterPlaylistUpdateSong && masterPlaylist != null) {
                    masterPlaylist.songEvent(song, event);
                }
            }
        }
    }
    
    /**
     * Returns <tt>true</tt> if the provided
     * <tt>song</tt> is in this Playlist.
     */
    public boolean contains(Song song) {
        return items.contains(song);
    }
    
    public synchronized Object select(DaapRequest request) {
        
        if (request.isPlaylistSongsRequest()) {
            
            if (request.isUpdateType()) {
                return playlistSongsUpdate;
                
            } else {
                return playlistSongs;
                
            }
        }
        
        if (LOG.isInfoEnabled()) {
            LOG.info("Unknown request: " + request);
        }
        
        return null;
    }
    
    /**
     * Destroys this Playlist. Used internally 
     * to speed up destruction
     */
    void destroy() {
        
        if (items != null) {
            items.clear();
            items = null;
        }
        
        if (newItems != null) {
            newItems.clear();
            newItems = null;
        }
        
        if (deletedItems != null) {
            deletedItems.clear();
            deletedItems = null;
        }
        
        if (properties != null) {
            properties.clear();
            properties = null;
        }
        
        playlistSongs = null;
        playlistSongsUpdate = null;
    }
    
    /**
     * Used internally. See Library.open()
     */
    void open() {
        newItems.clear();
        deletedItems.clear();
    }
    
    /**
     * Used internally. See Library.close()
     */
    void close() {
        
        playlistSongs 
            = (new PlaylistSongsImpl(items, newItems, deletedItems, false)).getBytes();
        
        playlistSongsUpdate 
            = (new PlaylistSongsImpl(items, newItems, deletedItems, true)).getBytes();
    }
    
    Playlist createSnapshot() {
        
        //Playlist clone = new Playlist(itemName.getValue(), itemId.getValue());
        Playlist clone = new Playlist(this);
        
        clone.playlistSongs = this.playlistSongs;
        clone.playlistSongsUpdate = this.playlistSongsUpdate;
        
        // Clones do not need this information...
        //clone.items = null;
        //clone.newItems = null;
        //clone.deletedItems = null;
        //clone.properties = null;
        
        return clone;
    }
    
    public String toString() {
        return getName();
    }
    
    /**
    * This class implements the PlaylistSongs
    */
    private final class PlaylistSongsImpl extends PlaylistSongs {
    
        private PlaylistSongsImpl(List items, List newItems, List deletedItems, boolean updateType) {
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
        
        public byte[] getBytes() {
            return getBytes(true);
        }
        
        public byte[] getBytes(boolean compress) {
            try {
                return DaapUtil.serialize(this, compress);
            } catch (IOException err) {
                LOG.error(err);
                return null;
            }
        }
    }
}
