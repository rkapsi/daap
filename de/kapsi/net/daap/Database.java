
package de.kapsi.net.daap;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.chunks.*;
import java.util.*;

public class Database implements SongListener {
	
	private static final Log LOG = LogFactory.getLog(Database.class);
	
	private int revision;
	private int id;
	private String name;
	private String persistentId;
	
	/** List of all tunes in our library */
	private ArrayList items = new ArrayList();
	private ArrayList newItems = new ArrayList();
	private ArrayList deletedItems = new ArrayList();
	
	/** List of playlists */
	private ArrayList containers = new ArrayList();
	private ArrayList deletedContainers = new ArrayList();
	
	/** main playlist */
	private Playlist mainPlaylist;
	
	/* friendly */
	Database(int id, String name, String persistentId) {
		
		this.id = id;
		this.name = name;
		this.persistentId = persistentId;
		
		this.mainPlaylist = new Playlist(name);
		containers.add(this.mainPlaylist);
		
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
	}
	
	public String getPersistentId() {
		return persistentId;
	}
	
	public List getItems(boolean updateType) {
		return (!updateType) ? items : newItems;
	}
	
	public List getDeletedItems() {
		return deletedItems;
	}
	
	public List getContainers() {
		return containers;
	}
	
	public void addSong(Song song) {
		if (items.contains(song)==false) {
			items.add(song);
			newItems.add(song);
			
			song.addListener(this);
			
			mainPlaylist.addSong(song);
			
			Integer id = new Integer(song.getContainerId());
			deletedItems.remove(id);
		}
	}
	
	public boolean removeSong(Song song) {
		if (items.remove(song)) {
			newItems.remove(song);
			song.removeListener(this);
			
			Integer id = new Integer(song.getContainerId());
			deletedItems.add(id);
			return true;
		}
		return false;
	}
	
	public void addPlaylist(Playlist playlist) {
		if (containers.contains(playlist)==false) {
			containers.add(playlist);
			
			playlist.addListener(this);
			
			Iterator it = playlist.getItems(false).iterator();
			while(it.hasNext()) {
				addSong((Song)it.next());
			}
		}
	}
	
	public boolean removePlaylist(Playlist playlist) {
		if (containers.remove(playlist)) {
			playlist.removeListener(this);
			deletedContainers.add(new Integer(playlist.getId()));
			return true;
		}
		return false;
	}
	
	public void songEvent(Song song, int event) {
	
		if (event == SongListener.SONG_CHANGED) {
			if (newItems.contains(song)==false) {
				newItems.add(song);
			}
		} else if (event == SongListener.SONG_ADDED) {
			addSong(song);
			
		} else if (event == SongListener.SONG_DELETED) {
			removeSong(song);
		}
	}
	
	void destroy() {
		items.clear();
		items = null;
		
		newItems.clear();
		newItems = null;
		
		deletedItems.clear();
		deletedItems = null;
		
		Iterator it = containers.iterator();
		while(it.hasNext()) {
			((Playlist)it.next()).destroy();
		}
		containers.clear();
		containers = null;
		
		deletedContainers.clear();
		deletedContainers = null;
	}
	
	private Song getSong(int itemId) {
		Iterator it = items.iterator();
		while(it.hasNext()) {
			Song song = (Song)it.next();
			if (song.getId()==itemId) {
				return song;
			}
		}
		return null;
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
	
	public String toString() {
		return "Name: " + getName() + ", revision: " + revision;
	}

	public synchronized Object select(DaapRequest request) {
		
		if (request.isSongRequest()) {
			return getSong(request.getItemId());
		
		} else if (request.isDatabaseSongsRequest()) {
			return getDatabaseSongs(request);
		
		} else if (request.isDatabasePlaylistsRequest()) {
			return getDatabasePlaylists(request);
		
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
	
	private DatabaseSongs getDatabaseSongs(DaapRequest request) {
		
		int revisionNumber = request.getRevisionNumber();
		int delta = request.getDelta();
		
		boolean updateType = (delta != DaapRequest.UNDEF_VALUE) && (delta < revisionNumber);
								
		DatabaseSongs databaseSongs = new DatabaseSongs();
		
		databaseSongs.add(new Status(200));
		databaseSongs.add(new UpdateType(updateType));
			
		int secifiedTotalCount = items.size()-deletedItems.size();
		int returnedCount = newItems.size();
		
		databaseSongs.add(new SpecifiedTotalCount(secifiedTotalCount));
		databaseSongs.add(new ReturnedCount(returnedCount));
			
		Listing listing = new Listing();
		
		Iterator it = getItems(updateType).iterator();
		
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
		
		databaseSongs.add(listing);
		
		if (updateType) {
		
			it = getDeletedItems().iterator();
			
			if (it.hasNext()) {
				
				DeletedIdListing deletedListing = new DeletedIdListing();
				
				while(it.hasNext()) {
					Integer itemId = (Integer)it.next();
					deletedListing.add(new ItemId(itemId.intValue()));
				}
		
				databaseSongs.add(deletedListing);
			}
		}
		
		return databaseSongs;
	}
	
	private DatabasePlaylists getDatabasePlaylists(DaapRequest request) {
		
		int revisionNumber = request.getRevisionNumber();
		int delta = request.getDelta();
		
		boolean updateType = (delta != DaapRequest.UNDEF_VALUE) && (delta < revisionNumber);
								
		DatabasePlaylists databasePlaylists = new DatabasePlaylists();
		
		databasePlaylists.add(new Status(200));
		databasePlaylists.add(new UpdateType(updateType));
		
		int specifiedTotalCount = containers.size()-deletedContainers.size();
		int returnedCount = specifiedTotalCount;
		
		databasePlaylists.add(new SpecifiedTotalCount(specifiedTotalCount));
		databasePlaylists.add(new ReturnedCount(returnedCount));
			
		Listing listing = new Listing();
		
		Iterator it = getContainers().iterator();
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
		
		databasePlaylists.add(listing);
			
		if (updateType) {
			
			it = deletedContainers.iterator();
			
			if (it.hasNext()) {
				
				DeletedIdListing deletedListing = new DeletedIdListing();
				
				while(it.hasNext()) {
					Integer itemId = (Integer)it.next();
					deletedListing.add(new ItemId(itemId.intValue()));
				}
		
				databasePlaylists.add(deletedListing);
			}
		}
		
		return databasePlaylists;
	}
	
	void createNewRevision() {
		revision++;
		newItems.clear();
		deletedItems.clear();
		deletedContainers.clear();
		
		Iterator it = containers.iterator();
		while(it.hasNext()) {
			((Playlist)it.next()).createNewRevision();
		}
	}
	
	Database createSnapshot() {
		
		Database clone = new Database();
		
		clone.revision = this.revision;
		clone.id = this.id;
		clone.name = this.name;
		clone.persistentId = this.persistentId;
		
		Iterator it = this.items.iterator();
		while(it.hasNext()) {
			clone.items.add((Song)it.next());
		}
		
		it = this.newItems.iterator();
		while(it.hasNext()) {
			clone.newItems.add(it.next());
		}
		
		it = this.deletedItems.iterator();
		while(it.hasNext()) {
			clone.deletedItems.add(it.next());
		}
		
		it = this.containers.iterator();
		while(it.hasNext()) {
			Playlist pl = (Playlist)it.next();
			
			Playlist npl = (Playlist)pl.createSnapshot();
			clone.containers.add(npl);
			
			if (npl.getId()==this.mainPlaylist.getId()) {
				clone.mainPlaylist = npl;
			}
		}
		
		it = this.deletedContainers.iterator();
		while(it.hasNext()) {
			clone.deletedContainers.add(it.next());
		}
		
		return clone;
	}
}