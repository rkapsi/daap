
package de.kapsi.net.daap;

import java.util.*;

import de.kapsi.net.daap.chunks.DatabaseSongsImpl;
import de.kapsi.net.daap.chunks.DatabasePlaylistsImpl;

import de.kapsi.net.daap.chunks.impl.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Database is used internally by Library and isn't accessible
 * from the outside
 */
public class Database {
    
    private static final Log LOG = LogFactory.getLog(Database.class);
    
    private int revision;
    private int id;
    private String name;
    private String persistentId;
    
    private DatabaseSongs databaseSongs;
    private DatabaseSongs databaseSongsUpdate;
    
    private DatabasePlaylists databasePlaylists;
    private DatabasePlaylists databasePlaylistsUpdate;
    
    /** List of playlists */
    private ArrayList containers;
    private ArrayList deletedContainers;
    
    /** master playlist */
    private Playlist masterPlaylist;
    
    /* friendly */
    Database(int id, String name, String persistentId) {
        
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
    private Database() {
    }
    
    public int getRevision() {
        return revision;
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        masterPlaylist.setName(name);
    }
    
    public String getPersistentId() {
        return persistentId;
    }
    
    public int size() {
        return masterPlaylist.size();
    }
    
    public Playlist getMasterPlaylist() {
        return masterPlaylist;
    }
    
    public List getPlaylists() {
        return containers;
    }
    
    public List getDeletedPlaylists() {
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
    
    public void add(Playlist playlist) {
        if (containers.contains(playlist)==false) {
            containers.add(playlist);
            playlist.setMasterPlaylist(masterPlaylist);
            
            deletedContainers.remove(new Integer(playlist.getId()));
        }
    }
    
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
    
    public String toString() {
        return "Name: " + getName() + ", revision: " + revision;
    }
    
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
    public void destroy() {
        
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
    public void open() {
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
    public void close() {
        
        List items = masterPlaylist.getSongs();
        List newItems = masterPlaylist.getNewSongs();
        List deletedItems = masterPlaylist.getDeletedSongs();
        
        databaseSongs = new DatabaseSongsImpl(items, newItems, deletedItems, false);
        databaseSongsUpdate = new DatabaseSongsImpl(items, newItems, deletedItems, true);
        
        databasePlaylists = new DatabasePlaylistsImpl(containers, deletedContainers, false);
        databasePlaylistsUpdate = new DatabasePlaylistsImpl(containers, deletedContainers, true);
        
        Iterator it = containers.iterator();
        while(it.hasNext()) {
            ((Playlist)it.next()).close();
        }
    }
    
    public Database createSnapshot() {
        
        Database clone = new Database();
        
        clone.revision = this.revision;
        clone.id = this.id;
        clone.name = this.name;
        clone.persistentId = this.persistentId;
        
        clone.databaseSongs = this.databaseSongs;
        clone.databaseSongsUpdate = this.databaseSongsUpdate;
        clone.databasePlaylists = this.databasePlaylists;
        clone.databasePlaylistsUpdate = this.databasePlaylistsUpdate;
        
        clone.containers = new ArrayList();
        Iterator it = this.containers.iterator();
        while(it.hasNext()) {
            clone.containers.add(((Playlist)it.next()).createSnapshot());
        }
        
        return clone;
    }
}