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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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

/**
 * The name is self explaining. A Playlist is a set of Songs.
 *
 * @author  Roger Kapsi
 */
public class Playlist implements Cloneable {
    
    private static final Log LOG = LogFactory.getLog(Playlist.class);
    
    private static int PLAYLIST_ID = 0;
    
    // Note: a playlist is "smart" when aeSP is in the serialized 
    // chunklist regardless of its value (i.e. true or false doesn't matter)
    private static final SmartPlaylist IS_SMART_PLAYLIST = new SmartPlaylist(!true);
    private static final DummyChunk IS_NOT_SMART_PLAYLIST = new DummyChunk(IS_SMART_PLAYLIST);
    
    private ItemId itemId;
    private ItemName itemName;
    private PersistentId persistentId;
    private ItemCount itemCount;
    
    private HashSet items;
    private HashSet newItems;
    private HashSet deletedItems;
    
    private HashMap properties;
    
    private byte[] playlistSongs;
    private byte[] playlistSongsUpdate;
    
    private Playlist masterPlaylist;
    private boolean isSmartPlaylist = false;
    
    private boolean notifyMasterPlaylistOnAdd = true;
    private boolean notifyMasterPlaylistOnRemove = false;
    private boolean notifyMasterPlaylistOnUpdate = true;
    
    /**
     * Creates a new Playlist
     * 
     * @param name the Name of the Playlist
     */
    public Playlist(String name) {
        this(name, true);
    }
    
    /**
     * Creates a new Playlist with the name and preloads the
     * Playlist with an empty Playlist data strcture if emptyPlaylist
     * is true.
     * 
     * @param name the Name of the Playlist
     * @param emptyPlaylist if <code>true</code> the Playlist
     *  will be preloaded with an empty Playlist data structure
     */
    public Playlist(String name, boolean emptyPlaylist) {
        
        synchronized(Playlist.class) {
            itemId = new ItemId(++PLAYLIST_ID);
        }
        
        items = new HashSet();
        newItems = new HashSet();
        deletedItems = new HashSet();
        properties = new HashMap();
        
        itemName = new ItemName(name);
        persistentId = new PersistentId(Library.nextPersistentId());
        itemCount = new ItemCount();
        
        add(itemId);
        add(itemName);
        add(persistentId);
        add(IS_NOT_SMART_PLAYLIST);
        add(itemCount);
        
        if (emptyPlaylist) {
            playlistSongs = new PlaylistSongsImpl(this, false).getBytes();
            playlistSongsUpdate = new PlaylistSongsImpl(this, true).getBytes();
        }
    }
    
    /**
     * Clone constructor
     * 
     * @param orig the original Playlist that shall be cloned
     */
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
            add(IS_SMART_PLAYLIST);
        } else {
            add(IS_NOT_SMART_PLAYLIST);
        }
        
        add(itemCount);
        
        playlistSongs = orig.playlistSongs;
        playlistSongsUpdate = orig.playlistSongsUpdate;
        
        notifyMasterPlaylistOnAdd = orig.notifyMasterPlaylistOnAdd;
        notifyMasterPlaylistOnRemove = orig.notifyMasterPlaylistOnRemove;
        notifyMasterPlaylistOnUpdate = orig.notifyMasterPlaylistOnUpdate;
    }
    
    Txn openTxn(Transaction txn) throws DaapException {
        if (!txn.isOpen()) {
            throw new DaapException("Transaction is not open");
        }
        
        PlaylistTxn obj = (PlaylistTxn)txn.getAttribute(this);
        if (obj == null) {
            obj = new PlaylistTxn(this);
            txn.setAttribute(this, obj);
        }
        return obj;
    }
    
    /**
     * Sets the master Playlist. This value is only valid
     * during a commit!
     * 
     * @param masterPlaylist a master Playlist
     */
    void setMasterPlaylist(Playlist masterPlaylist) {
        this.masterPlaylist = masterPlaylist;
    }
    
    /**
     * Returns the unique ID of this Playlist
     * 
     * @return unique ID of this Playlist
     */
    public int getId() {
        return itemId.getValue();
    }
    
    /**
     * 
     * @param chunk
     */
    private void add(AbstractChunk chunk) {
        properties.put(chunk.getName(), chunk);
    }
    
    /**
     * <b>This method does not exist! Ignore it! I'm serious! :)</b>
     * 
     * @param property a key (secret, only accessible for API developers)
     * @return a Chunk or <code>null</code>
     */
    public Chunk getProperty(String property) {
        return (Chunk)properties.get(property);
    }
    
    /**
     * Sets the name of this Playlist
     * 
     * @param txn a Transaction
     * @param name a new name
     * @throws DaapException
     */
    public void setName(Transaction txn, String name) throws DaapException {
        PlaylistTxn obj = (PlaylistTxn)openTxn(txn);
        obj.setName(name);
    }
    
    /**
     * Returns the name of this Playlist
     * 
     * @return the name of this Playlist
     */
    public String getName() {
        return itemName.getValue();
    }
    
    /**
     * If <code>true</code> (default) then add songs also to the 
     * master playlist
     * 
     * @param notify
     */
    public void setNotifyMasterPlaylistOnAdd(boolean notify) {
        notifyMasterPlaylistOnAdd = notify;
    }
    
    /**
     * If <code>false</code> (default) then remove songs also from 
     * the master playlist
     * 
     * @param notify
     */
    public void setNotifyMasterPlaylistOnRemove(boolean notify) {
        notifyMasterPlaylistOnRemove = notify;
    }
    
    /**
     * If <code>true</code> (default) then update songs also on the 
     * master playlist
     * 
     * @param notify
     */
    public void setNotifyMasterPlaylistOnUpdate(boolean notify) {
        notifyMasterPlaylistOnUpdate = notify;
    }
    
    /**
     * Sets whether or not this Playlist is a smart playlist.
     * The difference between smart and common playlists is that
     * smart playlists have a star as an icon and they appear
     * as first in the list.
     */
    public void setSmartPlaylist(Transaction txn, boolean smart) throws DaapException {
        PlaylistTxn obj = (PlaylistTxn)openTxn(txn);
        obj.setSmartPlaylist(smart);
    }
    
    /**
     * Returns <code>true</code> if this Playlist is a smart
     * playlist.
     */
    public boolean isSmartPlaylist() {
        return isSmartPlaylist;
    }
    
    /**
     * Returns a Song for the provided songId or
     * <code>null</code> if this id is unknown for
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
     * Returns true if playlist is empty
     * @return
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    /**
     * Returns the number of Songs in this Playlist
     */
    public int size() {
        return items.size();
    }
    
    /**
     * Retuns an unmodifiable set of all songs
     */
    public Set getSongs() {
        return Collections.unmodifiableSet(items);
    }
    
    /**
     * Returns an unmodifiable set of newly added songs.
     * Note: the contents of this Set is only valid during
     * a commit() and in the meantime it is empty.
     */
    public Set getNewSongs() {
        return Collections.unmodifiableSet(newItems);
    }
    
    /**
     * Retuens an unmodifiable set of newly deleted songs.
     * Note: the contents of this Set is only valid during
     * a commit() and in the meantime it is empty.
     */
    public Set getDeletedSongs() {
        return Collections.unmodifiableSet(deletedItems);
    }
    
    /**
     * Apply the new song attributes
     * 
     * @param song
     * @throws DaapTransactionException
     */
    public void update(Transaction txn, Song song) throws DaapException {
        if (!items.contains(song))
            return;
        
        PlaylistTxn obj = (PlaylistTxn)openTxn(txn);
        obj.update(song);
    }
    
    /**
     * Adds <code>song</code> to this Playlist
     * 
     * @param song
     * @throws DaapTransactionException
     */
    public void add(Transaction txn, Song song) throws DaapException {
        PlaylistTxn obj = (PlaylistTxn)openTxn(txn);
        obj.add(song);
    }
    
    /**
     * Removes <code>song</code> from this Playlist
     * 
     * @param song
     * @throws DaapTransactionException
     */
    public void remove(Transaction txn, Song song) throws DaapException {
        PlaylistTxn obj = (PlaylistTxn)openTxn(txn);
        obj.remove(song);
    }
    
    /**
     * Performs a select on this Playlist and returns 
     * something for the request or <code>null</code>
     * 
     * @param request a DaapRequest
     * @return a response for the DaapRequest
     */
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
     * Returns <code>true</code> if the provided
     * <code>song</code> is in this Playlist.
     */
    public boolean contains(Song song) {
        return items.contains(song);
    }
    
    public Object clone() throws CloneNotSupportedException {
        return new Playlist(this);
    }
    
    public String toString() {
        return getName();
    }
    
    /**
     * A Playlist specific implementation of Txn
     */
    private static class PlaylistTxn implements Txn {
        
        private Playlist playlist;
        
        private String name;
        private boolean isSmartPlaylist;
        
        private HashSet newItems = new HashSet();
        private HashSet deletedItems = new HashSet();
        private HashSet updateItems = new HashSet();
        
        private PlaylistTxn(Playlist playlist) {
            this.playlist = playlist;
            this.name = playlist.getName();
            this.isSmartPlaylist = playlist.isSmartPlaylist();
        }
        
        private void setName(String name) {
            this.name = name;
        }
        
        private void setSmartPlaylist(boolean smart) {
            isSmartPlaylist = smart;
        }
        
        private void add(Song song) {
            if (!newItems.contains(song)) {
                newItems.add(song);
                deletedItems.remove(song);
                updateItems.remove(song);
            }
        }
        
        private void remove(Song song) {
            if (!deletedItems.contains(song)) {
                deletedItems.add(song);
                newItems.remove(song);
                updateItems.remove(song);
            }
        }
        
        private void update(Song song) {
            if (!updateItems.contains(song) && !newItems.contains(song) && !deletedItems.contains(song)) {
                updateItems.add(song);
            }
        }
        
        public void commit(Transaction txn) {
            synchronized(playlist) {
                if (playlist.getName() != name) {
                    playlist.itemName.setValue(name);
                }
                
                if (playlist.isSmartPlaylist()  != isSmartPlaylist) {
                    playlist.isSmartPlaylist = isSmartPlaylist;
                    
                    if (isSmartPlaylist) {
                        playlist.add(IS_SMART_PLAYLIST);
                    } else {
                        playlist.add(IS_NOT_SMART_PLAYLIST);
                    }
                }
                
                Playlist masterPlaylist = playlist.masterPlaylist;
                
                Iterator it = newItems.iterator();
                while(it.hasNext()) {
                    Song song = (Song)it.next();
                    if (!playlist.items.contains(song)) {
                        playlist.items.add(song);
                        playlist.newItems.add(song);
                        playlist.deletedItems.remove(song);
                        
                        if (playlist.notifyMasterPlaylistOnAdd && playlist.masterPlaylist != null) {
                            playlist.masterPlaylist.add(txn, song);
                        }
                    }
                }
                
                it = updateItems.iterator();
                while(it.hasNext()) {
                    Song song = (Song)it.next();
                    if (playlist.items.contains(song) && !playlist.newItems.contains(song)) {
                        playlist.newItems.add(song);
                        
                        if (playlist.notifyMasterPlaylistOnUpdate && playlist.masterPlaylist != null) {
                            playlist.masterPlaylist.update(txn, song);
                        }
                    }
                }
                
                it = deletedItems.iterator();
                while(it.hasNext()) {
                    Song song = (Song)it.next();
                    if (playlist.items.remove(song)) {
                        playlist.newItems.remove(song);
                        playlist.deletedItems.add(song);
                        
                        if (playlist.notifyMasterPlaylistOnRemove && playlist.masterPlaylist != null) {
                            playlist.masterPlaylist.remove(txn, song);
                        }
                    }
                }
                
                // commit
                playlist.itemCount.setValue(playlist.items.size());
                
                playlist.playlistSongs = new PlaylistSongsImpl(playlist, false).getBytes();
                playlist.playlistSongsUpdate = new PlaylistSongsImpl(playlist, true).getBytes();
            }
            
            newItems.clear();
            deletedItems.clear();
            updateItems.clear();
        }
        
        public void rollback(Transaction txn) {
            newItems.clear();
            deletedItems.clear();
            updateItems.clear();
        }
        
        public void cleanup(Transaction txn) {
            synchronized(playlist) {
                playlist.newItems.clear();
                playlist.deletedItems.clear();
            }
            
            newItems.clear();
            deletedItems.clear();
            updateItems.clear();
        }
        
        public void join(Txn value) {
            PlaylistTxn obj = (PlaylistTxn)value;
            
            if (name != obj.name)
                name = obj.name;
            
            isSmartPlaylist = obj.isSmartPlaylist;
            
            Iterator it = obj.newItems.iterator();
            while(it.hasNext()) {
                add((Song)it.next());
            }
            
            it = obj.updateItems.iterator();
            while(it.hasNext()) {
                update((Song)it.next());
            }
            
            it = obj.deletedItems.iterator();
            while(it.hasNext()) {
                remove((Song)it.next());
            }
        }
        
        public String toString() {
            return "PlaylistTxn for " + playlist;
        }
    }
    
    /**
    * This class implements the PlaylistSongs
    */
    private static final class PlaylistSongsImpl extends PlaylistSongs {
    
        private PlaylistSongsImpl(Playlist playlist, boolean updateType) {
            super();

            add(new Status(200));
            add(new UpdateType(updateType));

            int secifiedTotalCount = playlist.items.size()-playlist.deletedItems.size();
            int returnedCount = playlist.newItems.size();

            add(new SpecifiedTotalCount(secifiedTotalCount));
            add(new ReturnedCount(returnedCount));

            Listing listing = new Listing();

            Iterator it = ((updateType) ? playlist.newItems : playlist.items).iterator();

            while(it.hasNext()) {
                ListingItem listingItem = new ListingItem();
                Song song = (Song)it.next();

                Iterator properties = Arrays.asList(
                        DaapUtil.PLAYLIST_SONGS_META).iterator();
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

                it = playlist.deletedItems.iterator();

                if (it.hasNext()) {

                    DeletedIdListing deletedListing = new DeletedIdListing();

                    while(it.hasNext()) {
                        Song song = (Song)it.next();
                        deletedListing.add(new ItemId(song.getId()));
                    }

                    add(deletedListing);
                }
            }
        }
        
        public byte[] getBytes() {
            return getBytes(DaapUtil.COMPRESS);
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
