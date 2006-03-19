/* 
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004, 2005 Roger Kapsi, info at kapsi dot de
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.chunks.Chunk;
import de.kapsi.net.daap.chunks.UIntChunk;
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
public class Database {

    private static final Log LOG = LogFactory.getLog(Database.class);
    
    /** databaseId is an 32bit unsigned value! */
    private static long databaseId = 1;
    
    /** unique id */
    private final long itemId;
    
    /** unique persistent id */
    private final long persistentId;

    /** Name of this Database */
    private String name;
    
    /** 
     * The total number of Playlists in this Database
     */
    private int totalPlaylistCount = 0;
    
    /**
     * The total number of Songs in this Database
     */
    private int totalSongCount = 0;
    
    /** A List of Playlists */
    private final List playlists = new ArrayList();
    
    /** Set of deleted playlists */
    private HashSet deletedPlaylists = null;
    
    /** Set of deleted Songs */
    private HashSet deletedSongs = null;
    
    /** master playlist */
    private Playlist masterPlaylist = null;
    
    protected Database(Database database, Transaction txn) {
        this.itemId = database.itemId;
        this.persistentId = database.persistentId;
        this.name = database.name;

        if (database.deletedPlaylists != null) {
            this.deletedPlaylists = database.deletedPlaylists;
            database.deletedPlaylists = null;
        }
        
        Set songs = database.getSongs();
        
        Iterator it = database.playlists.iterator();
        while(it.hasNext()) {
            Playlist playlist = (Playlist)it.next();
            
            if (txn.modified(playlist)) {
                if (deletedPlaylists == null || !deletedPlaylists.contains(playlist)) {
                    Playlist clone = new Playlist(playlist, txn);
                    playlists.add(clone);
                    
                    if (playlist == database.masterPlaylist) {
                        this.masterPlaylist = clone;
                    }
                }
                
                Set deletedSongs = playlist.getDeletedSongs();
                if (deletedSongs != null && !deletedSongs.isEmpty()) {
                    if (this.deletedSongs == null) {
                        this.deletedSongs = new HashSet(deletedSongs);
                    } else {
                        this.deletedSongs.addAll(deletedSongs);
                    }
                }
            }
        }
        
        if (deletedSongs != null) {
            deletedSongs.removeAll(songs);
        }
        
        this.totalPlaylistCount = database.playlists.size();
        this.totalSongCount = songs.size();
    }
    
    public Database(String name) {
        this(name, new Playlist(name));
    }
    
    /**
     * Create a new Database with the name
     * 
     * @param name a name for this Database
     */
    public Database(String name, Playlist masterPlaylist) {
        synchronized(Database.class) {
            this.itemId = UIntChunk.checkUIntRange(databaseId++);
        }
        
        this.persistentId = Library.nextPersistentId();
        this.name = name;
        this.totalPlaylistCount = 0;
        this.totalSongCount = 0;
        
        this.masterPlaylist = masterPlaylist;
        addPlaylistP(null, masterPlaylist);
    }
    
    /**
     * Returns the unique id of this Database
     * 
     * @return unique id of this Database
     */
    protected long getItemId() {
        return itemId;
    }

    /**
     * Returns the name of this Database.
     * 
     * @return name of this Database
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Database.
     * 
     * @param new name
     */
    public void setName(Transaction txn, final String name) {
        if (txn != null) {
            txn.addTxn(this, new Txn() {
                public void commit(Transaction txn) {
                    setNameP(txn, name);
                }
            });
        } else {
            setNameP(txn, name);
        }
        
        masterPlaylist.setName(txn, name);
    }

    private void setNameP(Transaction txn, String name) {
        this.name = name;
    }
    
    /**
     * The persistent id of this Database. Unused at the
     * moment!
     * 
     * @return the persistent id of this Database
     */
    protected long getPersistentId() {
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
    public List getPlaylists() {
        return Collections.unmodifiableList(playlists);
    }

    /**
     * Adds playlist to this Database
     * 
     * @param txn a Transaction
     * @param playlist the Playliost to add
     */
    public void addPlaylist(Transaction txn, final Playlist playlist) {
        if (masterPlaylist.equals(playlist)) {
            throw new DaapException("You cannot add the master playlist.");
        }
        
        if (txn != null) {
            txn.addTxn(this, new Txn() {
                public void commit(Transaction txn) {
                    addPlaylistP(txn, playlist);
                }
            });
            txn.attach(playlist);
        } else {
            addPlaylistP(txn, playlist);
        }
    }
    
    private void addPlaylistP(Transaction txn, Playlist playlist) {
        if (!containsPlaylist(playlist) && playlists.add(playlist)) {
            totalPlaylistCount = playlists.size();
            if (deletedPlaylists != null && deletedPlaylists.remove(playlist) 
                    && deletedPlaylists.isEmpty()) {
                deletedPlaylists = null;
            }
        }
    }
    
    /**
     * Removes playlist from this Database
     * 
     * @param txn a Transaction
     * @param playlist the Playlist to remove
     */
    public void removePlaylist(Transaction txn, final Playlist playlist) {
        if (masterPlaylist.equals(playlist)) {
            throw new DaapException("You cannot remove the master playlist.");
        }
        
        if (txn != null) {
            txn.addTxn(this, new Txn() {
                public void commit(Transaction txn) {
                    removePlaylistP(txn, playlist);
                }
            });
        } else {
            removePlaylistP(txn, playlist);
        }
    }

    private void removePlaylistP(Transaction txn, Playlist playlist) {
        if (playlists.remove(playlist)) {
            totalPlaylistCount = playlists.size();
            
            if (deletedPlaylists == null) {
                deletedPlaylists = new HashSet();
            }
            deletedPlaylists.add(playlist);
        }
    }
    
    /**
     * Returns the number of Playlists in this Database
     */
    public int getPlaylistCount() {
        return playlists.size();
    }
    
    /**
     * Returns true if playlist is in this Database
     * 
     * @param playlist
     * @return true if Database contains playlist
     */
    public boolean containsPlaylist(Playlist playlist) {
        return playlists.contains(playlist);
    }

    /**
     * Performs a select on this Database and returns 
     * something for the request or <code>null</code>
     * 
     * @param request a DaapRequest
     * @return a response for the DaapRequest
     */
    protected Object select(DaapRequest request) {

        if (request.isSongRequest()) {
            return getSong(request);

        } else if (request.isDatabaseSongsRequest()) {
            return getDatabaseSongs(request);

        } else if (request.isDatabasePlaylistsRequest()) {
            return getDatabasePlaylist(request);

        } else if (request.isPlaylistSongsRequest()) {

            Playlist playlist = getPlaylist(request);
            if (playlist == null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("No playlist " + request.getContainerId()
                            + " known in Database " + itemId);
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

    public String toString() {
        return "Database(" + getItemId() + ", " + getName() + ")";
    }
    
    public int hashCode() {
        return (int)(getItemId() & Integer.MAX_VALUE);
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof Database)) {
            return false;
        }
        return ((Database)o).getItemId() == getItemId();
    }
    
    /**
     * Returns all Songs in this Database
     */
    public Set getSongs() {
        HashSet songs = null;
        Iterator it = playlists.iterator();
        while(it.hasNext()) {
            Playlist playlist = (Playlist)it.next();
            if (!(playlist instanceof Folder)) {
                if (songs == null) {
                    songs = new HashSet(playlist.getSongs());
                } else {
                    songs.addAll(playlist.getSongs());
                }
            }
        }
        
        if (songs == null) {
            return Collections.EMPTY_SET;
        } else {
            return Collections.unmodifiableSet(songs);
        }
    }
    
    /**
     * Returns the number of Songs in this Database
     */
    public int getSongCount() {
        return getSongs().size();
    }
    
    /**
     * Returns true if song is in this Database
     */
    public boolean containsSong(Song song) {
        Iterator it = playlists.iterator();
        while(it.hasNext()) {
            Playlist playlist = (Playlist)it.next();
            if (playlist.containsSong(song)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Adds Song to all Playlists
     * 
     * @param txn
     * @param song
     */
    public void addSong(Transaction txn, Song song) {
        Iterator it = playlists.iterator();
        while(it.hasNext()) {
            Playlist playlist = (Playlist)it.next();
            if (!(playlist instanceof Folder)) {
                playlist.addSong(txn, song);
            }
        }
    }
    
    /**
     * Removes Song from all Playlists
     * 
     * @param txn
     * @param song
     */
    public void removeSong(Transaction txn, Song song) {
        Iterator it = playlists.iterator();
        while(it.hasNext()) {
            Playlist playlist = (Playlist)it.next();
            if (!(playlist instanceof Folder)) {
                playlist.removeSong(txn, song);
            }
        }
    }    
    
    public Set getSongPlaylists(Song song) {
        Set ret = null;
        Iterator it = playlists.iterator();
        while(it.hasNext()) {
            Playlist playlist = (Playlist)it.next();
            if (playlist.containsSong(song)) {
                if (ret == null) {
                    ret = new HashSet();
                }
                ret.add(playlist);
            }
        }
        
        return (ret != null) ? Collections.unmodifiableSet(ret) : Collections.EMPTY_SET;
    }
    
    /**
     * Gets and returns a Song by its ID
     * 
     * @param songId
     * @return
     */
    protected Song getSong(DaapRequest request) {
        Iterator it = playlists.iterator();
        while(it.hasNext()) {
            Playlist playlist = (Playlist)it.next();
            if (!(playlist instanceof Folder)) {
                Song song = playlist.getSong(request);
                if (song != null) {
                    return song;
                }
            }
        }
        return null;
    }
    
    /**
     * Gets and returns a Playlist by its ID
     * 
     * @param playlistId
     * @return
     */
    protected Playlist getPlaylist(DaapRequest request) {
        long playlistId = request.getContainerId();
        Iterator it = playlists.iterator();
        while (it.hasNext()) {
            Playlist playlist = (Playlist) it.next();
            if (playlist.getItemId() == playlistId) {
                return playlist;
            }
        }

        return null;
    }
    
    private DatabasePlaylists getDatabasePlaylist(DaapRequest request) {
        DatabasePlaylists databasePlaylists = new DatabasePlaylists();
        
        databasePlaylists.add(new Status(200));
        databasePlaylists.add(new UpdateType(request.isUpdateType() ? 1 : 0));
        
        databasePlaylists.add(new SpecifiedTotalCount(totalPlaylistCount));
        databasePlaylists.add(new ReturnedCount(playlists.size()));

        Listing listing = new Listing();

        Iterator it = playlists.iterator();
        while (it.hasNext()) {
            Playlist playlist = (Playlist) it.next();
            
            ListingItem listingItem = new ListingItem();
            
            Iterator meta = request.getMeta().iterator();
            while(meta.hasNext()) {
                String key = (String)meta.next();
                Chunk chunk = playlist.getChunk(key);
                
                if (chunk != null) {
                    listingItem.add(chunk);

                } else if (LOG.isInfoEnabled()) {
                    LOG.info("Unknown chunk type: " + key);
                }
            }
            
            listing.add(listingItem);
        }

        databasePlaylists.add(listing);

        if (request.isUpdateType() && deletedPlaylists != null) {
            DeletedIdListing deletedListing = new DeletedIdListing();
            
            it = deletedPlaylists.iterator();
            while (it.hasNext()) {
                Playlist playlist = (Playlist) it.next();
                deletedListing.add(new ItemId(playlist.getItemId()));
            }

            databasePlaylists.add(deletedListing);
        }
        
        return databasePlaylists;
    }
    
    private DatabaseSongs getDatabaseSongs(DaapRequest request) {
        DatabaseSongs databaseSongs = new DatabaseSongs();
        
        databaseSongs.add(new Status(200));
        databaseSongs.add(new UpdateType(request.isUpdateType() ? 1 : 0));
        databaseSongs.add(new SpecifiedTotalCount(totalSongCount));
        
        Set songs = getSongs();
        databaseSongs.add(new ReturnedCount(songs.size()));

        Listing listing = new Listing();
        
        Iterator it = songs.iterator();
        while (it.hasNext()) {
            ListingItem listingItem = new ListingItem();
            Song song = (Song)it.next();

            Iterator meta = request.getMeta().iterator();
            while (meta.hasNext()) {
                String key = (String)meta.next();
                Chunk chunk = song.getChunk(key);

                if (chunk != null) {
                    listingItem.add(chunk);

                } else if (LOG.isInfoEnabled()) {
                    LOG.info("Unknown chunk type: " + key);
                }
            }

            listing.add(listingItem);
        }

        databaseSongs.add(listing);
        
        if (request.isUpdateType() && deletedSongs != null) {
            DeletedIdListing deletedListing = new DeletedIdListing();
            
            it = deletedSongs.iterator();
            while (it.hasNext()) {
                Song song = (Song) it.next();
                deletedListing.add(song.getChunk("dmap.itemid"));
            }

            databaseSongs.add(deletedListing);
        }
        
        return databaseSongs;
    }
}