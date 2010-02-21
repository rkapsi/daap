/*
 * Digital Audio Access Protocol (DAAP)
 * Copyright (C) 2004-2010 Roger Kapsi
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

package org.ardverk.daap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.ardverk.daap.chunks.Chunk;
import org.ardverk.daap.chunks.impl.DatabasePlaylists;
import org.ardverk.daap.chunks.impl.DatabaseSongs;
import org.ardverk.daap.chunks.impl.DeletedIdListing;
import org.ardverk.daap.chunks.impl.ItemId;
import org.ardverk.daap.chunks.impl.Listing;
import org.ardverk.daap.chunks.impl.ListingItem;
import org.ardverk.daap.chunks.impl.ReturnedCount;
import org.ardverk.daap.chunks.impl.SpecifiedTotalCount;
import org.ardverk.daap.chunks.impl.Status;
import org.ardverk.daap.chunks.impl.UpdateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Database is a container for Playlists and it keeps track of all Songs in
 * the Database whereat it is not responsible for the actual management of the
 * Songs (it's only interested in the Song IDs).
 * 
 * @author Roger Kapsi
 */
public class Database {

    private static final Logger LOG = LoggerFactory.getLogger(Database.class);

    /** databaseId is an 32bit unsigned value! */
    private static final AtomicLong DATABASE_ID = new AtomicLong();

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
    private final List<Playlist> playlists = new ArrayList<Playlist>();

    /** Set of deleted playlists */
    private Set<Playlist> deletedPlaylists = null;

    /** Set of deleted Songs */
    private Set<Song> deletedSongs = null;

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

        Set<Song> songs = database.getSongs();

        for (Playlist playlist : database.playlists) {
            if (txn.modified(playlist)) {
                if (deletedPlaylists == null
                        || !deletedPlaylists.contains(playlist)) {
                    Playlist clone = new Playlist(playlist, txn);
                    playlists.add(clone);

                    if (playlist == database.masterPlaylist) {
                        this.masterPlaylist = clone;
                    }
                }

                Set<Song> deletedSongs = playlist.getDeletedSongs();
                if (deletedSongs != null && !deletedSongs.isEmpty()) {
                    if (this.deletedSongs == null) {
                        this.deletedSongs = new HashSet<Song>(deletedSongs);
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
     * @param name
     *            a name for this Database
     */
    public Database(String name, Playlist masterPlaylist) {
        this.itemId = DATABASE_ID.getAndIncrement();
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
     * The persistent id of this Database. Unused at the moment!
     * 
     * @return the persistent id of this Database
     */
    protected long getPersistentId() {
        return persistentId;
    }

    /**
     * Returns the master Playlist. The master Playlist is created automatically
     * by the Database! There's no technical difference between a master
     * Playlist and a usual Playlist except that it cannot be removed from the
     * Database.
     * 
     * @return the master Playlist
     */
    public Playlist getMasterPlaylist() {
        return masterPlaylist;
    }

    /**
     * Returns an unmodifiable Set with all Playlists in this Database
     * 
     * @return unmodifiable Set of Playlists
     */
    public List<Playlist> getPlaylists() {
        return Collections.unmodifiableList(playlists);
    }

    /**
     * Adds playlist to this Database
     * 
     * @param txn
     *            a Transaction
     * @param playlist
     *            the Playliost to add
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
     * @param txn
     *            a Transaction
     * @param playlist
     *            the Playlist to remove
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
                deletedPlaylists = new HashSet<Playlist>();
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
     * Performs a select on this Database and returns something for the request
     * or <code>null</code>
     * 
     * @param request
     *            a DaapRequest
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
        return (int) (getItemId() & Integer.MAX_VALUE);
    }

    public boolean equals(Object o) {
        if (!(o instanceof Database)) {
            return false;
        }
        return ((Database) o).getItemId() == getItemId();
    }

    /**
     * Returns all Songs in this Database
     */
    public Set<Song> getSongs() {
        Set<Song> songs = null;
        for (Playlist playlist : playlists) {
            if (!(playlist instanceof Folder)) {
                if (songs == null) {
                    songs = new HashSet<Song>(playlist.getSongs());
                } else {
                    songs.addAll(playlist.getSongs());
                }
            }
        }
        if (songs == null) {
            return Collections.emptySet();
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
        for (Playlist playlist : playlists) {
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
        for (Playlist playlist : playlists) {
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
        for (Playlist playlist : playlists) {
            if (!(playlist instanceof Folder)) {
                playlist.removeSong(txn, song);
            }
        }
    }

    public Set<Playlist> getSongPlaylists(Song song) {
        Set<Playlist> ret = null;
        for (Playlist playlist : playlists) {
            if (playlist.containsSong(song)) {
                if (ret == null) {
                    ret = new HashSet<Playlist>();
                }
                ret.add(playlist);
            }
        }

        if (ret != null) {
            return Collections.unmodifiableSet(ret);
        }

        return Collections.emptySet();
    }

    /**
     * Gets and returns a Song by its ID
     * 
     * @param songId
     * @return
     */
    protected Song getSong(DaapRequest request) {
        for (Playlist playlist : playlists) {
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
        for (Playlist playlist : playlists) {
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

        for (Playlist playlist : playlists) {
            ListingItem listingItem = new ListingItem();

            for (String key : request.getMeta()) {
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

            for (Playlist playlist : deletedPlaylists) {
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

        Set<Song> songs = getSongs();
        databaseSongs.add(new ReturnedCount(songs.size()));

        Listing listing = new Listing();

        for (Song song : songs) {
            ListingItem listingItem = new ListingItem();

            for (String key : request.getMeta()) {
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

            for (Song song : deletedSongs) {
                deletedListing.add(song.getChunk("dmap.itemid"));
            }

            databaseSongs.add(deletedListing);
        }

        return databaseSongs;
    }
}