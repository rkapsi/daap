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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.chunks.Chunk;
import de.kapsi.net.daap.chunks.impl.DatabasePlaylists;
import de.kapsi.net.daap.chunks.impl.DatabaseSongs;
import de.kapsi.net.daap.chunks.impl.DeletedIdListing;
import de.kapsi.net.daap.chunks.impl.ItemId;
import de.kapsi.net.daap.chunks.impl.Listing;
import de.kapsi.net.daap.chunks.impl.ListingItem;
import de.kapsi.net.daap.chunks.impl.ReturnedCount;
import de.kapsi.net.daap.chunks.impl.SpecifiedTotalCount;
import de.kapsi.net.daap.chunks.impl.Status;
import de.kapsi.net.daap.chunks.impl.UpdateType;

/**
 * A Database is a container for Playlists and it keeps track of 
 * all Songs in the Database whereat it is not responsible for
 * the actual management of the Songs (it's only interested in 
 * the Song IDs).
 *  
 * @author Roger Kapsi
 */
public class Database implements Cloneable {

    private static final Log LOG = LogFactory.getLog(Database.class);

    private static int ID = 0;
    private int id;

    private long persistentId;
    private byte[] databaseSongs;
    private byte[] databaseSongsUpdate;
    private byte[] databasePlaylists;
    private byte[] databasePlaylistsUpdate;

    /** List of playlists */
    private HashSet containers;
    
    /** List of deleted playlists */
    private HashSet deletedContainers;

    /** master playlist */
    private Playlist masterPlaylist;

    /**
     * Create a new Database with the name
     * 
     * @param name a name for this Database
     */
    public Database(String name) {

        synchronized (Database.class) {
            this.id = ++ID;
        }

        this.persistentId = Library.nextPersistentId();

        containers = new HashSet();
        deletedContainers = new HashSet();

        masterPlaylist = new Playlist(name);
        containers.add(masterPlaylist);
    }

    private Database(Database orig) throws CloneNotSupportedException {

        id = orig.id;
        persistentId = orig.persistentId;

        databaseSongs = orig.databaseSongs;
        databaseSongsUpdate = orig.databaseSongsUpdate;
        databasePlaylists = orig.databasePlaylists;
        databasePlaylistsUpdate = orig.databasePlaylistsUpdate;

        containers = new HashSet();

        Iterator it = orig.containers.iterator();
        while (it.hasNext()) {
            Playlist playlist = (Playlist) it.next();
            Playlist clone = (Playlist) playlist.clone();

            if (playlist == orig.masterPlaylist)
                masterPlaylist = clone;

            containers.add(clone);
        }
    }

    /**
     * Returns the unique id of this Database
     * 
     * @return unique id of this Database
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the name of this Database. Same as
     * Database.getMasterPlaylist().getName()
     * 
     * @return name of this Database
     */
    public String getName() {
        return masterPlaylist.getName();
    }

    /**
     * Sets the name of this Database. Same as
     * Database.getMasterPlaylist().setName(String)
     * 
     * @param name the new name of the master Playlist
     */
    public void setName(Transaction txn, String name) {
        masterPlaylist.setName(txn, name);
    }

    /**
     * The persistent id of this Database. Unused at the
     * moment!
     * 
     * @return the persistent id of this Database
     */
    long getPersistentId() {
        return persistentId;
    }

    /**
     * Returns the master Playlist. The master Playlist
     * is created automatically by the Database! There's
     * no technical difference between a master Playlist
     * and a usual Playlist except that it cannot be
     * removed from the Database.
     * 
     * @return the master Playlist
     */
    public Playlist getMasterPlaylist() {
        return masterPlaylist;
    }

    /**
     * Returns an unmodifiable Set with all Playlists
     * in this Database
     * 
     * @return unmodifiable Set of Playlists
     */
    public Set getPlaylists() {
        return Collections.unmodifiableSet(containers);
    }

    /**
     * Returns an unmodifiable Set with all deleted Playlists.
     * <p>NOTE: only valid during a {@see Transaction.commit()}
     * and always empty in the meantime.</p>
     * 
     * @return unmodifiable Set of deleted Playlists
     */
    public Set getDeletedPlaylists() {
        return Collections.unmodifiableSet(deletedContainers);
    }

    /**
     * 
     * @param txn
     * @return
     * @throws DaapException
     */
    Txn openTxn(Transaction txn) throws DaapException {
        if (!txn.isOpen()) {
            throw new DaapException("Transaction is not open");
        }
        
        DatabaseTxn obj = (DatabaseTxn)txn.getAttribute(this);
        if (obj == null) {
            obj = new DatabaseTxn(this);
            txn.setAttribute(this, obj);
        }
        return obj;
    }

    /**
     * Adds playlist to this Database
     * 
     * @param txn a Transaction
     * @param playlist the Playliost to add
     * @throws DaapException
     */
    public void add(Transaction txn, Playlist playlist) throws DaapException {
        if (playlist == masterPlaylist)
            throw new DaapException("You cannot add the master playlist.");

        DatabaseTxn obj = (DatabaseTxn)openTxn(txn);
        obj.add(playlist);
    }

    /**
     * Removes playlist from this Database
     * 
     * @param txn a Transaction
     * @param playlist the Playlist to remove
     * @throws DaapException
     */
    public void remove(Transaction txn, Playlist playlist) throws DaapException {
        if (playlist == masterPlaylist)
            throw new DaapException("You cannot remove the master playlist.");

        DatabaseTxn obj = (DatabaseTxn)openTxn(txn);
        obj.remove(playlist);
    }

    /**
     * Performs an update operation on all playlists which contain
     * this song
     * 
     * @param txn a Transaction
     * @param song the Song to be updated in all Playlists
     * @throws DaapException
     */
    public void update(Transaction txn, Song song) throws DaapException {
        DatabaseTxn obj = (DatabaseTxn)openTxn(txn);
        obj.update(song);
    }
    
    /**
     * Adds Song to all Playlists of this Database
     * 
     * @param txn a Transaction
     * @param song the Song to be added
     * @throws DaapException
     */
    public void add(Transaction txn, Song song) throws DaapException {
        DatabaseTxn obj = (DatabaseTxn)openTxn(txn);
        obj.add(song);
    }

    /**
     * Removes song from all playlists of this Database
     * 
     * @param txn a Transaction
     * @param song the Song to be removed from all Playlists
     * @throws DaapException
     */
    public void remove(Transaction txn, Song song) throws DaapException {
        DatabaseTxn obj = (DatabaseTxn)openTxn(txn);
        obj.remove(song);
    }

    /**
     * Returns true if Database contains no Playlists
     * 
     * @return true if Database contains no Playlists
     */
    public boolean isEmpty() {
        return containers.isEmpty();
    }
    
    /**
     * Returns the number of Playlists in this Database
     * 
     * @return the number of Playlists in this Database
     */
    public int size() {
        return containers.size();
    }
    
    /**
     * Returns true if playlist is in this Database
     * 
     * @param playlist
     * @return true if Database contains playlist
     */
    public boolean contains(Playlist playlist) {
        return containers.contains(playlist);
    }
    
    /**
     * Gets and returns a Playlist by its ID
     * 
     * @param playlistId
     * @return
     */
    private Playlist getPlaylist(int playlistId) {
        Iterator it = containers.iterator();
        while (it.hasNext()) {
            Playlist pl = (Playlist) it.next();
            if (pl.getId() == playlistId) {
                return pl;
            }
        }

        return null;
    }

    /**
     * Gets and returns a Song by its ID
     * 
     * @param songId
     * @return
     */
    private Song getSong(int songId) {
        Iterator it = containers.iterator();
        while (it.hasNext()) {
            Playlist playlist = (Playlist) it.next();
            Song song = playlist.getSong(songId);
            if (song != null)
                return song;
        }
        return null;
    }

    /**
     * Performs a select on this Database and returns 
     * something for the request or <code>null</code>
     * 
     * @param request a DaapRequest
     * @return a response for the DaapRequest
     */
    public synchronized Object select(DaapRequest request) {

        if (request.isSongRequest()) {
            return getSong(request.getItemId());

        } else if (request.isDatabaseSongsRequest()) {

            if (request.isUpdateType()) {
                return databaseSongsUpdate;
            } else {
                return databaseSongs;
            }

        } else if (request.isDatabasePlaylistsRequest()) {

            if (request.isUpdateType()) {
                return databasePlaylistsUpdate;
            } else {
                return databasePlaylists;
            }

        } else if (request.isPlaylistSongsRequest()) {

            Playlist playlist = getPlaylist(request.getContainerId());
            if (playlist == null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("No playlist " + request.getContainerId()
                            + " known in Database " + id);
                }
                return null;
            }

            return playlist.select(request);
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("Unknown request: " + request);
        }

        return null;
    }

    public Object clone() throws CloneNotSupportedException {
        return new Database(this);
    }

    public String toString() {
        return "Database(" + getId() + ", " + getName() + ")";
    }

    /**
     * A Txn implementation for Databases
     */
    private static class DatabaseTxn implements Txn {

        private Database database;
        
        private HashSet newItems = new HashSet();
        private HashSet deletedItems = new HashSet();
        private HashSet updateItems = new HashSet();
        
        private HashSet containers = new HashSet();
        private HashSet deletedContainers = new HashSet();

        private DatabaseTxn(Database database) {
            this.database = database;
        }

        private void add(Playlist playlist) {
            if (!containers.contains(playlist)) {
                containers.add(playlist);
                deletedContainers.remove(playlist);
            }
        }

        private void remove(Playlist playlist) {
            if (!deletedContainers.contains(playlist)) {
                deletedContainers.add(playlist);
                containers.remove(playlist);
            }
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
            if (!updateItems.contains(song) 
                    && !newItems.contains(song) 
                    && !deletedItems.contains(song)) {
                updateItems.add(song);
            }
        }

        public void commit(Transaction txn) {
            synchronized(database) {
                Iterator it = null;
    
                it = containers.iterator();
                while (it.hasNext()) {
                    Playlist playlist = (Playlist) it.next();
                    if (!database.containers.contains(playlist)) {
                        database.containers.add(playlist);
                        database.deletedContainers.remove(playlist);
                    }
                }
    
                it = deletedContainers.iterator();
                while (it.hasNext()) {
                    Playlist playlist = (Playlist) it.next();
                    if (database.containers.remove(playlist)) {
                        database.deletedContainers.add(playlist);
                        playlist.setMasterPlaylist(null);
                    }
                }
    
                it = database.containers.iterator();
                while (it.hasNext()) {
                    Playlist playlist = (Playlist) it.next();
                    
                    Iterator add = newItems.iterator();
                    while (add.hasNext()) {
                        Song song = (Song) add.next();
                        playlist.add(txn, song);
                    }
                    
                    Iterator update = updateItems.iterator();
                    while (update.hasNext()) {
                        Song song = (Song) update.next();
                        playlist.update(txn, song);
                    }
                    
                    Iterator remove = deletedItems.iterator();
                    while (remove.hasNext()) {
                        Song song = (Song) remove.next();
                        playlist.remove(txn, song);
                    }
                }
                
                // commit
                it = database.containers.iterator();
                while (it.hasNext()) {
                    Playlist playlist = (Playlist) it.next();
                    if (playlist != database.masterPlaylist) {
                        Txn obj = txn.getAttribute(playlist);
                        if (obj != null) {
                            playlist.setMasterPlaylist(database.masterPlaylist);
                            obj.commit(txn);
                        }
                    }
                }
                
                Txn obj = txn.getAttribute(database.masterPlaylist);
                if (obj != null)
                    obj.commit(txn);

                database.databaseSongs = new DatabaseSongsImpl(database.masterPlaylist, false).getBytes();
                database.databaseSongsUpdate = new DatabaseSongsImpl(database.masterPlaylist, true)
                        .getBytes();

                database.databasePlaylists = new DatabasePlaylistsImpl(database, false).getBytes();
                database.databasePlaylistsUpdate = new DatabasePlaylistsImpl(database, true)
                        .getBytes();
            }
            
            containers.clear();
            deletedContainers.clear();
            updateItems.clear();
        }

        public void rollback(Transaction txn) {
            synchronized(database) {
                Iterator it = containers.iterator();
                while (it.hasNext()) {
                    Playlist playlist = (Playlist) it.next();
                    if (playlist != database.masterPlaylist) {
                        Txn obj = txn.getAttribute(playlist);
                        if (obj != null)
                            obj.rollback(txn);
                    }
                }
                
                Txn obj = txn.getAttribute(database.masterPlaylist);
                if (obj != null)
                    obj.rollback(txn);
            }
            
            containers.clear();
            deletedContainers.clear();
            updateItems.clear();
        }
        
        public void cleanup(Transaction txn) {
            synchronized(database) {
                Iterator it = containers.iterator();
                while (it.hasNext()) {
                    Playlist playlist = (Playlist) it.next();
                    Txn obj = txn.getAttribute(playlist);
                    if (obj != null)
                        obj.cleanup(txn);
                }
                
                database.deletedContainers.clear();
            }
            
            containers.clear();
            deletedContainers.clear();
            updateItems.clear();
        }
        
        public void join(Txn value) {
            DatabaseTxn obj = (DatabaseTxn)value;
            
            // Songs
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
            
            // Playlists
            it = obj.containers.iterator();
            while(it.hasNext()) {
                add((Playlist)it.next());
            }
            
            it = obj.deletedContainers.iterator();
            while(it.hasNext()) {
                remove((Playlist)it.next());
            }
        }
        
        public String toString() {
            return "DatabaseTxn for " + database;
        }
    }

    /**
     * This class is an implementation of DatabasePlaylists
     */
    private static final class DatabasePlaylistsImpl extends DatabasePlaylists {

        private DatabasePlaylistsImpl(Database database, boolean updateType) {
            super();

            add(new Status(200));
            add(new UpdateType(updateType));

            int specifiedTotalCount = database.containers.size()
                    - database.deletedContainers.size();
            int returnedCount = specifiedTotalCount;

            add(new SpecifiedTotalCount(specifiedTotalCount));
            add(new ReturnedCount(returnedCount));

            Listing listing = new Listing();

            Playlist masterPlaylist = database.getMasterPlaylist();

            // The only difference between master and general playlists
            // is that the master playlist is the 1st in the serialized
            // list!
            listing.add(toListingItem(masterPlaylist));

            Iterator it = database.containers.iterator();
            while (it.hasNext()) {
                Playlist playlist = (Playlist) it.next();
                if (playlist != masterPlaylist) {
                    listing.add(toListingItem(playlist));
                }
            }

            add(listing);

            if (updateType) {

                it = database.deletedContainers.iterator();

                if (it.hasNext()) {

                    DeletedIdListing deletedListing = new DeletedIdListing();

                    while (it.hasNext()) {
                        Playlist playlist = (Playlist) it.next();
                        deletedListing.add(new ItemId(playlist.getId()));
                    }

                    add(deletedListing);
                }
            }
        }

        private ListingItem toListingItem(Playlist playlist) {
            ListingItem listingItem = new ListingItem();

            Iterator properties = Arrays.asList(
                    DaapUtil.DATABASE_PLAYLISTS_META).iterator();
            while (properties.hasNext()) {
                String key = (String) properties.next();
                Chunk chunk = playlist.getProperty(key);

                if (chunk != null) {
                    listingItem.add(chunk);

                } else if (LOG.isInfoEnabled()) {
                    LOG.info("Unknown chunk type: " + key);
                }
            }

            return listingItem;
        }

        private byte[] getBytes() {
            return getBytes(DaapUtil.COMPRESS);
        }

        private byte[] getBytes(boolean compress) {
            try {
                return DaapUtil.serialize(this, compress);
            } catch (IOException err) {
                LOG.error(err);
                return null;
            }
        }
    }

    /**
     * This class is an implementation of DatabaseSongs
     */
    private static final class DatabaseSongsImpl extends DatabaseSongs {

        /**
         * 
         * @param playlist
         * @param updateType
         */
        private DatabaseSongsImpl(Playlist playlist, boolean updateType) {
            super();

            Set items = playlist.getSongs();
            Set newItems = playlist.getNewSongs();
            Set deletedItems = playlist.getDeletedSongs();

            add(new Status(200));
            add(new UpdateType(updateType));

            int secifiedTotalCount = items.size() - deletedItems.size();
            int returnedCount = newItems.size();

            add(new SpecifiedTotalCount(secifiedTotalCount));
            add(new ReturnedCount(returnedCount));

            Listing listing = new Listing();

            Iterator it = ((updateType) ? newItems : items).iterator();

            while (it.hasNext()) {
                ListingItem listingItem = new ListingItem();
                Song song = (Song) it.next();

                Iterator properties = Arrays.asList(
                        DaapUtil.DATABASE_SONGS_META).iterator();
                while (properties.hasNext()) {

                    String key = (String) properties.next();
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

                    while (it.hasNext()) {
                        Song song = (Song) it.next();
                        deletedListing.add(new ItemId(song.getId()));
                    }

                    add(deletedListing);
                }
            }
        }

        /**
         * 
         * @return
         */
        public byte[] getBytes() {
            return getBytes(DaapUtil.COMPRESS);
        }

        /**
         * 
         * @param compress
         * @return
         */
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