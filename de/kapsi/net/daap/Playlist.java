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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Collections;

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
    
    public Playlist(String name) {
        
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
    }
    
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
    
    void commit() throws DaapTransactionException {
        if (!DaapTransaction.isOpen()) {
            throw new DaapTransactionException("Current Thread is not associated with a transaction.");
        }
        
        DaapTransaction transaction = DaapTransaction.getTransaction();
        PlaylistTransaction obj = (PlaylistTransaction)transaction.getAttribute(this);
        
        if (obj != null) {
            obj.commit();
        }
        
        itemCount.setValue(items.size());
        
        playlistSongs = (new PlaylistSongsImpl(this, false)).getBytes();
        playlistSongsUpdate = (new PlaylistSongsImpl(this, true)).getBytes();
    }
    
    void cleanup() {
        newItems.clear();
        deletedItems.clear();
    }
    
    void rollback() throws DaapTransactionException {
        if (!DaapTransaction.isOpen()) {
            throw new DaapTransactionException("Current Thread is not associated with a transaction.");
        }
        
        DaapTransaction transaction = DaapTransaction.getTransaction();
        PlaylistTransaction obj = (PlaylistTransaction)transaction.getAttribute(this);
        
        if (obj != null) {
            obj.rollback();
        }
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
        if (!DaapTransaction.isOpen()) {
            throw new DaapTransactionException("Current Thread is not associated with a transaction.");
        }
        
        DaapTransaction transaction = DaapTransaction.getTransaction();
        PlaylistTransaction obj = (PlaylistTransaction)transaction.getAttribute(this);
        
        if (obj == null) {
            obj = new PlaylistTransaction(this);
            transaction.setAttribute(this, obj);
        }
        
        obj.setName(name);
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
        if (!DaapTransaction.isOpen()) {
            throw new DaapTransactionException("Current Thread is not associated with a transaction.");
        }
        
        DaapTransaction transaction = DaapTransaction.getTransaction();
        PlaylistTransaction obj = (PlaylistTransaction)transaction.getAttribute(this);
        
        if (obj == null) {
            obj = new PlaylistTransaction(this);
            transaction.setAttribute(this, obj);
        }
        
        obj.setSmartPlaylist(smart);
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
    public void update(Song song) throws DaapTransactionException {
        if (!DaapTransaction.isOpen()) {
            throw new DaapTransactionException();
        }
        
        if (!items.contains(song))
            return;
        
        DaapTransaction transaction = DaapTransaction.getTransaction();
        PlaylistTransaction obj = (PlaylistTransaction)transaction.getAttribute(this);
        
        if (obj == null) {
            obj = new PlaylistTransaction(this);
            transaction.setAttribute(this, obj);
        }
        
        obj.update(song);
    }
    
    /**
     * Adds <tt>song</tt> to this Playlist
     * 
     * @param song
     * @throws DaapTransactionException
     */
    public void add(Song song) throws DaapTransactionException {
        if (!DaapTransaction.isOpen()) {
            throw new DaapTransactionException("Current Thread is not associated with a transaction.");
        }
        
        DaapTransaction transaction = DaapTransaction.getTransaction();
        PlaylistTransaction obj = (PlaylistTransaction)transaction.getAttribute(this);
        
        if (obj == null) {
            obj = new PlaylistTransaction(this);
            transaction.setAttribute(this, obj);
        }
        
        obj.add(song);
    }
    
    /**
     * Removes <tt>song</tt> from this Playlist
     * and returns <tt>true</tt>
     * 
     * @param song
     * @throws DaapTransactionException
     */
    public void remove(Song song) throws DaapTransactionException {
        
        if (!DaapTransaction.isOpen()) {
            throw new DaapTransactionException("Current Thread is not associated with a transaction.");
        }
        
        DaapTransaction transaction = DaapTransaction.getTransaction();
        PlaylistTransaction obj = (PlaylistTransaction)transaction.getAttribute(this);
        
        if (obj == null) {
            obj = new PlaylistTransaction(this);
            transaction.setAttribute(this, obj);
        }
        
        obj.remove(song);
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
     * Returns <tt>true</tt> if the provided
     * <tt>song</tt> is in this Playlist.
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
    
    private static class PlaylistTransaction {
        
        private Playlist playlist;
        
        private String name;
        private boolean isSmartPlaylist;
        
        private HashSet newItems = new HashSet();
        private HashSet deletedItems = new HashSet();
        private HashSet updateItems = new HashSet();
        
        private PlaylistTransaction(Playlist playlist) {
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
        
        private void commit() {
            
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
                    
                    if (playlist.notifyMasterPlaylistOnAdd && masterPlaylist != null) {
                        if (!masterPlaylist.items.contains(song)) {
                            masterPlaylist.items.add(song);
                            masterPlaylist.newItems.add(song);
                            masterPlaylist.deletedItems.remove(song);
                        }
                    }
                }
            }
            
            it = deletedItems.iterator();
            while(it.hasNext()) {
                Song song = (Song)it.next();
                if (playlist.items.remove(song)) {
                    playlist.newItems.remove(song);
                    playlist.deletedItems.add(song);
                    
                    if (playlist.notifyMasterPlaylistOnRemove && masterPlaylist != null) {
                        if (masterPlaylist.items.remove(song)) {
                            masterPlaylist.newItems.remove(song);
                            masterPlaylist.deletedItems.add(song);
                        }
                    }
                }
            }
            
            it = updateItems.iterator();
            while(it.hasNext()) {
                Song song = (Song)it.next();
                if (playlist.items.contains(song) && !playlist.newItems.contains(song)) {
                    playlist.newItems.add(song);
                    
                    if (playlist.notifyMasterPlaylistOnUpdate && masterPlaylist != null) {
                        if (masterPlaylist.items.contains(song) && !masterPlaylist.newItems.contains(song)) {
                            masterPlaylist.newItems.add(song);
                        }
                    }
                }
            }
            
            newItems.clear();
            deletedItems.clear();
            updateItems.clear();
        }
        
        private void rollback() {
            newItems.clear();
            deletedItems.clear();
            updateItems.clear();
        }
    }
    
    /**
    * This class implements the PlaylistSongs
    */
    private final class PlaylistSongsImpl extends PlaylistSongs {
    
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
                        Song song = (Song)it.next();
                        deletedListing.add(new ItemId(song.getId()));
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
