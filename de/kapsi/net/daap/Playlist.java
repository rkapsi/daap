
package de.kapsi.net.daap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.chunks.*;

import java.util.*;

public class Playlist implements SongListener {
	
	private static final Log LOG = LogFactory.getLog(Playlist.class);
	
	private static int PLAYLIST_ID = 1;

	private ItemId itemId;
	private ItemName itemName;
	//private final ItemCount itemCount = new ItemCount(0);
	
	private PersistentId persistentId;
	private SmartPlaylist smartPlaylist;
	//private final ContainerItemId containerItemId = new ContainerItemId();
	
	private ArrayList items = new ArrayList();
	private ArrayList newItems = new ArrayList();
	private ArrayList deletedItems = new ArrayList();
	
	private HashMap properties = new HashMap();
	private HashSet listener = new HashSet();
		
	public Playlist(String name) {
		
		synchronized(Playlist.class) {
			itemId = new ItemId(PLAYLIST_ID++);
		}
		
		itemName = new ItemName(name);
		persistentId = new PersistentId(itemId.getValue());
		smartPlaylist = new SmartPlaylist(false);
		
		add(itemId);
		add(itemName);
		//add(itemCount);
		add(persistentId);
		//add(smartPlaylist);
		//add(containerItemId);
	}
	
	// required for createSnapshot()
	private Playlist(int id, String name) {
		
		this.itemId = new ItemId(id);
		this.itemName = new ItemName(name);
		
		persistentId = new PersistentId(itemId.getValue());
		smartPlaylist = new SmartPlaylist(false);
		
		add(itemId);
		add(itemName);
		//add(itemCount);
		add(persistentId);
		//add(smartPlaylist);
		//add(containerItemId);
	}
	
	public int getId() {
		return itemId.getValue();
	}
	
	private void add(AbstractChunk chunk) {
		properties.put(chunk.getChunkName(), chunk);
	}
	
	Chunk getProperty(String property) {
		return (Chunk)properties.get(property);
	}
	
	void addListener(SongListener l) {
		listener.add(l);
	}
	
	void removeListener(SongListener l) {
		listener.remove(l);
	}
	
	public void setName(String name) {
		itemName.setValue(name);
	}
	
	public String getName() {
		return itemName.getValue();
	}
	
	public List getItems(boolean updateType) {
		return (!updateType) ? items : newItems;
	}
	
	public List getDeletedItems() {
		return deletedItems;
	}
	
	public void addSong(Song song) {
		
		if (items.contains(song)==false) {
			
			items.add(song);
			newItems.add(song);
			song.addListener(this);
			
			Integer id = new Integer(song.getContainerId());
			deletedItems.remove(id);
			
			Iterator it = listener.iterator();
			while(it.hasNext()) {
				((SongListener)it.next()).songEvent(song, SongListener.SONG_ADDED);
			}
		}
	}
	
	public boolean removeSong(Song song) {
		
		if (items.remove(song)) {
		
			song.removeListener(this);
			newItems.remove(song);
			
			Integer id = new Integer(song.getContainerId());
			deletedItems.add(id);
			
			Iterator it = listener.iterator();
			while(it.hasNext()) {
				((SongListener)it.next()).songEvent(song, SongListener.SONG_DELETED);
			}
			
			return true;
		}
		
		return false;
	}
	
	public void songEvent(Song song, int event) {
		if (event == SongListener.SONG_CHANGED) {
			if (newItems.contains(song)==false) {
				newItems.add(song);
			}
		}
	}
	
	void destroy() {
		
		items.clear();
		items = null;
		
		newItems.clear();
		newItems = null;
		
		deletedItems.clear();
		deletedItems = null;
		
		properties.clear();
		properties = null;
		
		listener.clear();
		listener = null;
	}
	
	public boolean contains(Song song) {
		return items.contains(song);
	}
	
	public synchronized Object select(DaapRequest request) {
		
		if (request.isPlaylistSongsRequest()) {
			return getPlaylistSongs(request);
		}
		
		if (LOG.isInfoEnabled()) {
			LOG.info("Unknown request: " + request);
		}
		
		return null;
	}
	
	private PlaylistSongs getPlaylistSongs(DaapRequest request) {
		
		int revisionNumber = request.getRevisionNumber();
		int delta = request.getDelta();
		
		boolean updateType = (delta != DaapRequest.UNDEF_VALUE) && (delta < revisionNumber);
		
		PlaylistSongs playlistSongs = new PlaylistSongs();
		
		playlistSongs.add(new Status(200));
		playlistSongs.add(new UpdateType(updateType));
	
		int secifiedTotalCount = items.size()-deletedItems.size();
		int returnedCount = newItems.size();
		
		playlistSongs.add(new SpecifiedTotalCount(secifiedTotalCount));
		playlistSongs.add(new ReturnedCount(returnedCount));
		
		Listing listing = new Listing();
		
		Iterator it = getItems(updateType).iterator();
		
		while(it.hasNext()) {
			ListingItem listingItem = new ListingItem();
			Song song = (Song)it.next();
			
			Iterator properties = new ArrayIterator(DaapUtil.PLAYLIST_SONGS_META);
			while(properties.hasNext()) {
				String key = (String)properties.next();
				
				Chunk chunk = song.getProperty(key);
				
				if (chunk != null) {
					listingItem.add(chunk);
				/*} else if (key.equals("dmap.containeritemid")) {
					chunk = playlist.getProperty(key);
					
					if (chunk != null) {
						listingItem.add(chunk);
					} else if (LOG.isErrorEnabled()) {
						LOG.error(key + " not definied in Playlist!");
					}*/
				} else if (LOG.isInfoEnabled()) {
					LOG.info("Unknown chunk type: " + key);
				}
			}
			
			listing.add(listingItem);
		}
	
		playlistSongs.add(listing);
		
		if (updateType) {
		
			it = getDeletedItems().iterator();
			
			if (it.hasNext()) {
				
				DeletedIdListing deletedListing = new DeletedIdListing();
				
				while(it.hasNext()) {
					Integer itemId = (Integer)it.next();
					deletedListing.add(new ItemId(itemId.intValue()));
				}
		
				playlistSongs.add(deletedListing);
			}
		}
		
		return playlistSongs;
	}
	
	void createNewRevision() {
		newItems.clear();
		deletedItems.clear();
	}
	
	Playlist createSnapshot() {
	
		Playlist clone = new Playlist(this.itemId.getValue(), this.itemName.getValue());
		
		Iterator it = this.items.iterator();
		while(it.hasNext()) {
			clone.items.add(it.next());
		}
		
		it = this.newItems.iterator();
		while(it.hasNext()) {
			clone.newItems.add(it.next());
		}
		
		it = this.deletedItems.iterator();
		while(it.hasNext()) {
			clone.deletedItems.add(it.next());
		}
		
		return clone;
	}
}
