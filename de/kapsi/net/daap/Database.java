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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Collections;

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
import de.kapsi.util.ArrayIterator;

/**
 * 
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
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the name of this Database. Same as
     * Database.getMasterPlaylist().getName()
     * 
     * @return
     */
    public String getName() {
        return masterPlaylist.getName();
    }

    /**
     * Sets the name of this Database. Same as
     * Database.getMasterPlaylist().setName(String)
     * 
     * @param name
     */
    public void setName(String name) {
        masterPlaylist.setName(name);
    }

    long getPersistentId() {
        return persistentId;
    }

    /**
     * Returns the master playlist
     * @return
     */
    public Playlist getMasterPlaylist() {
        return masterPlaylist;
    }

    public Set getPlaylists() {
        return Collections.unmodifiableSet(containers);
    }

    public Set getDeletedPlaylists() {
        return Collections.unmodifiableSet(deletedContainers);
    }

    void commit() throws DaapTransactionException {
        if (!DaapTransaction.isOpen()) {
            throw new DaapTransactionException(
                    "Current Thread is not associated with a transaction.");
        }

        DaapTransaction transaction = DaapTransaction.getTransaction();
        DatabaseTransaction obj = (DatabaseTransaction) transaction
                .getAttribute(this);

        if (obj != null) {
            obj.commit();
        }

        Iterator it = containers.iterator();
        while (it.hasNext()) {
            Playlist playlist = (Playlist) it.next();
            if (playlist != masterPlaylist) {
                playlist.setMasterPlaylist(masterPlaylist);
                playlist.commit();
            }
        }

        masterPlaylist.commit();

        databaseSongs = new DatabaseSongsImpl(masterPlaylist, false).getBytes();
        databaseSongsUpdate = new DatabaseSongsImpl(masterPlaylist, true)
                .getBytes();

        databasePlaylists = new DatabasePlaylistsImpl(this, false).getBytes();
        databasePlaylistsUpdate = new DatabasePlaylistsImpl(this, true)
                .getBytes();
    }

    void cleanup() {
        Iterator it = containers.iterator();
        while (it.hasNext()) {
            Playlist playlist = (Playlist) it.next();
            playlist.cleanup();
        }
    }

    void rollback() throws DaapTransactionException {
        if (!DaapTransaction.isOpen()) {
            throw new DaapTransactionException(
                    "Current Thread is not associated with a transaction.");
        }

        DaapTransaction transaction = DaapTransaction.getTransaction();
        DatabaseTransaction obj = (DatabaseTransaction) transaction
                .getAttribute(this);

        if (obj != null) {
            obj.rollback();
        }

        Iterator it = containers.iterator();
        while (it.hasNext()) {
            Playlist playlist = (Playlist) it.next();
            if (playlist != masterPlaylist) {
                playlist.rollback();
            }
        }

        masterPlaylist.rollback();
    }

    /**
     * Adds playlist to this Database
     * 
     * @param playlist
     * @throws DaapTransactionException
     */
    public void add(Playlist playlist) throws DaapTransactionException {
        if (!DaapTransaction.isOpen()) {
            throw new DaapTransactionException(
                    "Current Thread is not associated with a transaction.");
        }

        if (playlist == masterPlaylist)
            throw new RuntimeException("You cannot add the master playlist.");

        DaapTransaction transaction = DaapTransaction.getTransaction();
        DatabaseTransaction obj = (DatabaseTransaction) transaction
                .getAttribute(this);

        if (obj == null) {
            obj = new DatabaseTransaction(this);
            transaction.setAttribute(this, obj);
        }

        obj.add(playlist);
    }

    /**
     * Removes playlist from this Database
     * 
     * @param playlist
     * @return
     * @throws DaapTransactionException
     */
    public void remove(Playlist playlist) throws DaapTransactionException {
        if (!DaapTransaction.isOpen()) {
            throw new DaapTransactionException(
                    "Current Thread is not associated with a transaction.");
        }

        if (playlist == masterPlaylist)
            throw new RuntimeException("You cannot remove the master playlist.");

        DaapTransaction transaction = DaapTransaction.getTransaction();
        DatabaseTransaction obj = (DatabaseTransaction) transaction
                .getAttribute(this);

        if (obj == null) {
            obj = new DatabaseTransaction(this);
            transaction.setAttribute(this, obj);
        }

        obj.remove(playlist);
    }

    /**
     * Performs an update operation on all playlists which contain
     * this song
     * 
     * @param song
     * @throws DaapTransactionException
     */
    public void update(Song song) throws DaapTransactionException {
        if (!DaapTransaction.isOpen()) {
            throw new DaapTransactionException(
                    "Current Thread is not associated with a transaction.");
        }

        DaapTransaction transaction = DaapTransaction.getTransaction();
        DatabaseTransaction obj = (DatabaseTransaction) transaction
                .getAttribute(this);

        if (obj == null) {
            obj = new DatabaseTransaction(this);
            transaction.setAttribute(this, obj);
        }

        obj.update(song);
    }
    
    /**
     * Adds song to all playlists of this Database
     * 
     * @param song
     */
    public void add(Song song) throws DaapTransactionException {
        if (!DaapTransaction.isOpen()) {
            throw new DaapTransactionException(
                    "Current Thread is not associated with a transaction.");
        }

        DaapTransaction transaction = DaapTransaction.getTransaction();
        DatabaseTransaction obj = (DatabaseTransaction) transaction
                .getAttribute(this);

        if (obj == null) {
            obj = new DatabaseTransaction(this);
            transaction.setAttribute(this, obj);
        }

        obj.add(song);
    }

    /**
     * Removes song from all playlists of this Database
     * 
     * @param song
     */
    public void remove(Song song) throws DaapTransactionException {
        if (!DaapTransaction.isOpen()) {
            throw new DaapTransactionException(
                    "Current Thread is not associated with a transaction.");
        }

        DaapTransaction transaction = DaapTransaction.getTransaction();
        DatabaseTransaction obj = (DatabaseTransaction) transaction
                .getAttribute(this);

        if (obj == null) {
            obj = new DatabaseTransaction(this);
            transaction.setAttribute(this, obj);
        }

        obj.remove(song);
    }

    /**
     * Returns true if Database is empty
     * @return
     */
    public boolean isEmpty() {
        return containers.isEmpty();
    }
    
    /**
     * Returns the number of playlists
     * @return
     */
    public int size() {
        return containers.size();
    }
    
    /**
     * Returns true if playlist is in this Database
     * @param playlist
     * @return
     */
    public boolean contains(Playlist playlist) {
        return containers.contains(playlist);
    }
    
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
     * 
     * @param request
     * @return
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

    private static class DatabaseTransaction {

        private Database database;
        
        private HashSet newItems = new HashSet();
        private HashSet deletedItems = new HashSet();
        private HashSet updateItems = new HashSet();
        
        private HashSet containers = new HashSet();
        private HashSet deletedContainers = new HashSet();

        private DatabaseTransaction(Database database) {
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

        private void commit() {
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
                if (database.containers.remove(playlist))
                    database.deletedContainers.add(playlist);
            }

            it = database.containers.iterator();
            while (it.hasNext()) {
                Playlist playlist = (Playlist) it.next();
                
                Iterator add = newItems.iterator();
                while (add.hasNext()) {
                    Song song = (Song) add.next();
                    playlist.add(song);
                }
                
                Iterator remove = deletedItems.iterator();
                while (remove.hasNext()) {
                    Song song = (Song) remove.next();
                    playlist.remove(song);
                }
                
                Iterator update = updateItems.iterator();
                while (update.hasNext()) {
                    Song song = (Song) update.next();
                    playlist.update(song);
                }
            }

            containers.clear();
            deletedContainers.clear();
            updateItems.clear();
        }

        private void rollback() {
            containers.clear();
            deletedContainers.clear();
            updateItems.clear();
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

            Iterator properties = new ArrayIterator(
                    DaapUtil.DATABASE_PLAYLISTS_META);
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
            return getBytes(true);
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

                Iterator properties = new ArrayIterator(
                        DaapUtil.DATABASE_SONGS_META);
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
            return getBytes(true);
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