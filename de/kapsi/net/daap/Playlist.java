
package de.kapsi.net.daap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.chunks.Chunk;
import de.kapsi.net.daap.chunks.AbstractChunk;
import de.kapsi.net.daap.chunks.PlaylistSongsImpl;

import de.kapsi.net.daap.chunks.impl.ItemId;
import de.kapsi.net.daap.chunks.impl.ItemName;
import de.kapsi.net.daap.chunks.impl.PersistentId;
import de.kapsi.net.daap.chunks.impl.PlaylistSongs;
//import de.kapsi.net.daap.chunks.impl.ItemCount;
//import de.kapsi.net.daap.chunks.impl.SmartPlaylist;
//import de.kapsi.net.daap.chunks.impl.ContainerItemId;

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
    
    private ItemId itemId;
    private ItemName itemName;
    //private ItemCount itemCount;
    
    private PersistentId persistentId;
    //private SmartPlaylist smartPlaylist;
    //private final ContainerItemId containerItemId = new ContainerItemId();
    
    private ArrayList items;
    private ArrayList newItems;
    private ArrayList deletedItems;
    
    private HashMap properties;
    
    private PlaylistSongs playlistSongs;
    private PlaylistSongs playlistSongsUpdate;
    
    private Playlist masterPlaylist;
    
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
        persistentId = new PersistentId(itemId.getValue());
        //smartPlaylist = new SmartPlaylist(true);
        //itemCount = new ItemCount(0);
        
        add(itemId);
        add(itemName);
        //add(itemCount);
        add(persistentId);
        //add(smartPlaylist);
        //add(containerItemId);
    }
    
    // required for createSnapshot()
    private Playlist(String name, int id) {
        
        properties = new HashMap();
        
        this.itemId = new ItemId(id);
        this.itemName = new ItemName(name);
        
        persistentId = new PersistentId(itemId.getValue());
        //smartPlaylist = new SmartPlaylist(false);
        
        add(itemId);
        add(itemName);
        //add(itemCount);
        add(persistentId);
        //add(smartPlaylist);
        //add(containerItemId);
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
    
    /*public void setSmartPlaylist(boolean smart) {
        smartPlaylist.setValue(smart);
    }
     
    public boolean isSmartPlaylist() {
        return smartPlaylist.getValue();
    }*/
    
    /**
     * Returns a Song for the provided songId or
     * <tt>null</tt> if this id is unknown for
     * this Playlist
     */
    public Song getSong(int songId) {
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
    public List getSongs() {
        return items;
    }
    
    /**
     * Used internally by Database
     */
    public List getNewSongs() {
        return newItems;
    }
    
    /**
     * Used internally by Database
     */
    public List getDeletedSongs() {
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
            = new PlaylistSongsImpl(items, newItems, deletedItems, false);
        
        playlistSongsUpdate 
            = new PlaylistSongsImpl(items, newItems, deletedItems, true);
    }
    
    Playlist createSnapshot() {
        
        Playlist clone = new Playlist(itemName.getValue(), itemId.getValue());
        
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
}
