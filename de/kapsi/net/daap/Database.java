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
import java.util.Iterator;
import java.util.List;

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
 * Database is used internally by Library and isn't accessible
 * from the outside
 *
 * @author  Roger Kapsi
 */
public class Database {
    
    private static final Log LOG = LogFactory.getLog(Database.class);
    
    private int revision;
    private int id;
    private String name;
    private long persistentId;
    
    private byte[] databaseSongs;
    private byte[] databaseSongsUpdate;
    
    private byte[] databasePlaylists;
    private byte[] databasePlaylistsUpdate;
    
    /** List of playlists */
    private ArrayList containers;
    private ArrayList deletedContainers;
    
    /** master playlist */
    private Playlist masterPlaylist;
    
    /* friendly */
    Database(int id, String name, long persistentId) {
        
        this.id = id;
        this.name = name;
        this.persistentId = persistentId;
        
        containers = new ArrayList();
        deletedContainers = new ArrayList();
        
        masterPlaylist = new Playlist(name);
        containers.add(masterPlaylist);
        
        this.revision = 1;
    }
    
    // required for createSnapshot()
    private Database(Database orig) {
        
        revision = orig.revision;
        id = orig.id;
        name = orig.name;
        persistentId = orig.persistentId;
        
        databaseSongs = orig.databaseSongs;
        databaseSongsUpdate = orig.databaseSongsUpdate;
        databasePlaylists = orig.databasePlaylists;
        databasePlaylistsUpdate = orig.databasePlaylistsUpdate;
        
        containers = new ArrayList();
        Iterator it = orig.containers.iterator();
        while(it.hasNext()) {
            containers.add(((Playlist)it.next()).createSnapshot());
        }
    }
    
    /**
     *
     * @return
     */    
    public int getRevision() {
        return revision;
    }
    
    /**
     *
     * @return
     */    
    public int getId() {
        return id;
    }
    
    /**
     *
     * @return
     */    
    public String getName() {
        return name;
    }
    
    /**
     *
     * @param name
     */    
    public void setName(String name) {
        this.name = name;
        masterPlaylist.setName(name);
    }
    
    long getPersistentId() {
        return persistentId;
    }
    
    /**
     *
     * @return
     */    
    public int size() {
        return masterPlaylist.size();
    }
    
    Playlist getMasterPlaylist() {
        return masterPlaylist;
    }
    
    List getPlaylists() {
        return containers;
    }
    
    List getDeletedPlaylists() {
        return deletedContainers;
    }
    
    /**
     * Adds the Song to the Master Playlist
     */
    public void add(Song song) {
        masterPlaylist.add(song);
    }
    
    /**
     * Removes the Song from the Master Playlist
     * and from all other Playlists
     */
    boolean remove(Song song) {
        if (masterPlaylist.remove(song)) {
            Iterator it = containers.iterator();
            while(it.hasNext()) {
                Playlist pl = (Playlist)it.next();
                if (pl != masterPlaylist)
                    pl.remove(song);
            }
            return true;
        }
        return false;
    }
    
    /**
     *
     * @param playlist
     */    
    public void add(Playlist playlist) {
        if (containers.contains(playlist)==false) {
            containers.add(playlist);
            playlist.setMasterPlaylist(masterPlaylist);
            
            deletedContainers.remove(new Integer(playlist.getId()));
        }
    }
    
    /**
     *
     * @param playlist
     * @return
     */    
    public boolean remove(Playlist playlist) {
        if (containers.remove(playlist)) {
            deletedContainers.add(new Integer(playlist.getId()));
            playlist.setMasterPlaylist(null);
            
            return true;
        }
        return false;
    }
    
    private Playlist getPlaylist(int playlistId) {
        
        Iterator it = containers.iterator();
        while(it.hasNext()) {
            Playlist pl = (Playlist)it.next();
            if (pl.getId()==playlistId) {
                return pl;
            }
        }
        
        return null;
    }
    
    private Song getSong(int songId) {
        return masterPlaylist.getSong(songId);
    }
    
    /**
     *
     * @return
     */    
    public String toString() {
        return "Name: " + getName() + ", revision: " + revision;
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
    
    /**
     * Used to speed up the destruction process
     */
    void destroy() {
        
        if (containers != null) {
            Iterator it = containers.iterator();
            while(it.hasNext()) {
                ((Playlist)it.next()).destroy();
            }
            
            containers.clear();
            containers = null;
        }
        
        if (deletedContainers != null) {
            deletedContainers.clear();
            deletedContainers = null;
        }
        
        databaseSongs = null;
        databaseSongsUpdate = null;
        
        databasePlaylists = null;
        databasePlaylistsUpdate = null;
    }
    
    /**
     * Used internally. See Library.open()
     */
    void open() {
        revision++;
        
        deletedContainers.clear();
        
        Iterator it = containers.iterator();
        while(it.hasNext()) {
            ((Playlist)it.next()).open();
        }
    }
    
    /**
     * Used internally. See Library.open()
     */
    void close() {
        
        List items = masterPlaylist.getSongs();
        List newItems = masterPlaylist.getNewSongs();
        List deletedItems = masterPlaylist.getDeletedSongs();
        
        databaseSongs = (new DatabaseSongsImpl(items, newItems, deletedItems, false)).getBytes();
        databaseSongsUpdate = (new DatabaseSongsImpl(items, newItems, deletedItems, true)).getBytes();
        
        databasePlaylists = (new DatabasePlaylistsImpl(containers, deletedContainers, false)).getBytes();
        databasePlaylistsUpdate = (new DatabasePlaylistsImpl(containers, deletedContainers, true)).getBytes();
        
        Iterator it = containers.iterator();
        while(it.hasNext()) {
            ((Playlist)it.next()).close();
        }
    }
    
    /**
     *
     * @return
     */    
    Database createSnapshot() {
        
        return new Database(this);
    }
    
    /**
    * This class is an implementation of DatabasePlaylists
    */
    private final class DatabasePlaylistsImpl extends DatabasePlaylists {

        private DatabasePlaylistsImpl(List containers, List deletedContainers, boolean updateType) {
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
    
    /**
    * This class is an implementation of DatabaseSongs
    */
    public final class DatabaseSongsImpl extends DatabaseSongs {
        
        /**
         *
         * @param items
         * @param newItems
         * @param deletedItems
         * @param updateType
         */        
        public DatabaseSongsImpl(List items, List newItems, List deletedItems, boolean updateType) {
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

                Iterator properties = new ArrayIterator(DaapUtil.DATABASE_SONGS_META);
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
