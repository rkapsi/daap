/* 
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004 Roger Kapsi, info at kapsi dot de
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.kapsi.net.daap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.chunks.AbstractChunk;
import de.kapsi.net.daap.chunks.Chunk;
import de.kapsi.net.daap.chunks.DummyChunk;
import de.kapsi.net.daap.chunks.impl.DeletedIdListing;
import de.kapsi.net.daap.chunks.impl.ItemCount;
import de.kapsi.net.daap.chunks.impl.ItemId;
import de.kapsi.net.daap.chunks.impl.ItemName;
import de.kapsi.net.daap.chunks.impl.Listing;
import de.kapsi.net.daap.chunks.impl.ListingItem;
import de.kapsi.net.daap.chunks.impl.PersistentId;
import de.kapsi.net.daap.chunks.impl.PlaylistSongs;
import de.kapsi.net.daap.chunks.impl.ReturnedCount;
import de.kapsi.net.daap.chunks.impl.SmartPlaylist;
import de.kapsi.net.daap.chunks.impl.SpecifiedTotalCount;
import de.kapsi.net.daap.chunks.impl.Status;
import de.kapsi.net.daap.chunks.impl.UpdateType;
import de.kapsi.util.ArrayIterator;

/**
 * The name is self explaining.
 *
 * @author  Roger Kapsi
 */
public class Playlist implements SongListener {
    
    private static final Log LOG = LogFactory.getLog(Playlist.class);
    
    private static int PLAYLIST_ID = 0;
    
    // Note: a playlist is "smart" when aeSP is in the serialized 
    // chunklist regardless of its value (i.e. true or false doesn't matter)
    private static final SmartPlaylist SMART_PLAYLIST = new SmartPlaylist(!true);
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
    
    private boolean notifyMasterPlaylistOnAdd = true;
    private boolean notifyMasterPlaylistOnRemove = false;
    private boolean notifyMasterPlaylistOnUpdate = true;
    
    public Playlist(String name) {
        
        synchronized(Playlist.class) {
            itemId = new ItemId(++PLAYLIST_ID);
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
        
        if (orig.isSmartPlaylist()) {
            add(SMART_PLAYLIST);
        } else {
            add(DUMMY_CHUNK);
        }
        
        add(itemCount);
        
        playlistSongs = orig.playlistSongs;
        playlistSongsUpdate = orig.playlistSongsUpdate;
        
        notifyMasterPlaylistOnAdd = orig.notifyMasterPlaylistOnAdd;
        notifyMasterPlaylistOnRemove = orig.notifyMasterPlaylistOnRemove;
        notifyMasterPlaylistOnUpdate = orig.notifyMasterPlaylistOnUpdate;
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
    
    /**
     * If <tt>true</tt> (default) then add songs also to the 
     * master playlist
     * @param notify
     */
    public void setNotifyMasterPlaylistOnAdd(boolean notify) {
        notifyMasterPlaylistOnAdd = notify;
    }
    
    /**
     * If <tt>false</tt> (default) then remove songs also from 
     * the master playlist
     * @param notify
     */
    public void setNotifyMasterPlaylistOnRemove(boolean notify) {
        notifyMasterPlaylistOnRemove = notify;
    }
    
    /**
     * If <tt>true</tt> (default) then update songs also on the 
     * master playlist
     * @param notify
     */
    public void setNotifyMasterPlaylistOnUpdate(boolean notify) {
        notifyMasterPlaylistOnUpdate = notify;
    }
    
    /**
     * Sets whether or not this Playlist is a smart playlist.
     * The difference between smart and common playlists is that
     * smart playlists have a star as an icon and they appear
     * at first in the list.
     */
    public void setSmartPlaylist(boolean smart) {
        isSmartPlaylist = smart;
        
        if (isSmartPlaylist) {
            add(SMART_PLAYLIST);
        } else {
            add(DUMMY_CHUNK);
        }
    }
    
    /**
     * Returns <tt>true</tt> if this Playlist is a smart
     * playlist.
     */
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
            
            if (notifyMasterPlaylistOnAdd && masterPlaylist != null) {
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
            
            if (notifyMasterPlaylistOnRemove && masterPlaylist != null) {
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
                
                if (notifyMasterPlaylistOnUpdate && masterPlaylist != null) {
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
        
        return new Playlist(this);
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
