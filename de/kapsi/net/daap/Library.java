
package de.kapsi.net.daap;

import org.apache.commons.httpclient.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.chunks.*;

import java.util.*;

public class Library {
	
	private static final int DATABASE_ID = 1;
	
	private static final Log LOG = LogFactory.getLog(Library.class);
	
	private ArrayList revisions = new ArrayList();
	
	private int keepNumRevisions;
	private String name;
	
	private Database current;
	private Database temp;
	
	private boolean open = false;
	
	public Library(String name) {
		this(name, 10);
	}

	public Library(String name, int keepNumRevisions) {
		this.name = name;
		this.keepNumRevisions = keepNumRevisions;
	}
	
	private int getRevision() {
		if (current == null) {
			return 0;
		} else {
			return current.getRevision();
		}
	}
	
	public void setName(String name) {
		if (!open) {
			throw new IllegalStateException();
		}
		
		temp.setName(name);
	}
	
	public String getName() {
		if (current == null) {
			return name;
		} else {
			return current.getName();
		}
	}
	
	public synchronized boolean isOpen() {
		return open;
	}
	
	public synchronized void open() {
		
		if (open) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Library is already opened for edit");
			}
			return;
		}
		
		if (current == null) {
			// current is initialized on close()! 
			temp = new Database(DATABASE_ID, name, "0");
			
		} else {
			
			temp = current;
			current = temp.createSnapshot();
			
			temp.createNewRevision();
		}
		
		open = true;
	}
	
	public synchronized void close() {
		if (!open) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Library wasn't opened for edit");
			}
			return;
		}
		
		if (current != null) {
			revisions.add(current);
		}

		current = temp;
		temp = null;
		
		open = false;
		
		if (revisions.size() >= keepNumRevisions) {
			Database old = (Database)revisions.remove(0);
			old.destroy();
		}
	}
	
	public synchronized Object select(DaapRequest request) {
	
		int revisionNumber = request.getRevisionNumber();
		int delta = request.getDelta();
		
		if (request.isUpdateRequest()) {
			
			// What's the next revision of the database 
			// iTunes should ask for?
			if (delta == DaapRequest.UNDEF_VALUE) { // 1st. request, iTunes should/will 
													// ask for the current revision
				return (new Integer(getRevision()));
				
			} else if (delta < getRevision()) {
				return (new Integer(++delta)); // ask for the 
												// next revision
				
			} else {
			
				// iTunes is up-to-date
				return (new Integer(delta));
			}
			
		} else if (request.isDatabasesRequest()) {
			
			Database database = getDatabase(request);
			
			if (database == null) {
				
				if (LOG.isInfoEnabled()) {
					LOG.info("No database with this revision known: " + revisionNumber);
				}
				
				return null;
			}
		
			return getServerDatabases(request, database);
			
		} else if (request.isSongRequest() 
					|| request.isDatabaseSongsRequest() 
					|| request.isDatabasePlaylistsRequest()
					|| request.isPlaylistSongsRequest()) {
			
			Database database = getDatabase(request);
			if (database == null) {
				if (LOG.isInfoEnabled()) {
					LOG.info("No database with this revision known: " + revisionNumber);
				}
				
				return null;
			}
			
			return database.select(request);
		
		} else {
			if (LOG.isInfoEnabled()) {
				LOG.info("Unknown request: " + request);
			}
			return null;
		}
	}
	
	private Database getDatabase(DaapRequest request) {
		
		if (current == null) {
			return null;
		}
		
		int revisionNumber = request.getRevisionNumber();
		if (revisionNumber == DaapRequest.UNDEF_VALUE || revisionNumber == current.getRevision()) {
			return current;
		}
		
		Iterator it = revisions.iterator();
		while(it.hasNext()) {
			Database database = (Database)it.next();
			if (database.getRevision() == revisionNumber) {
				return database;
			}
		}
		
		return null;
	}
	
	public void addSong(Song song) {
		if (!open) {
			throw new IllegalStateException();
		}
		
		temp.addSong(song);
	}
	
	public boolean removeSong(Song song) {
	
		if (!open) {
			throw new IllegalStateException();
		}
		
		if (temp.removeSong(song)) {
			return true;
		}
		
		return false;
	}
	
	public void addPlaylist(Playlist playlist) {
		
		if (!open) {
			throw new IllegalStateException();
		}
		
		temp.addPlaylist(playlist);
	}
	
	public boolean removePlaylist(Playlist playlist) {
	
		if (!open) {
			throw new IllegalStateException();
		}
		
		return temp.removePlaylist(playlist);
	}
	
	private ServerDatabases getServerDatabases(DaapRequest request, Database database) {
		
		int revisionNumber = request.getRevisionNumber();
		int delta = request.getDelta();
		
		boolean updateType = (delta != DaapRequest.UNDEF_VALUE) // i.e. 1st request (no update)
								&& (delta < revisionNumber);	// iTunes already knows this revision!?
																// (maybe an error!)
		
		ServerDatabases serverDatabases = new ServerDatabases();
		
		serverDatabases.add(new Status(200));
		serverDatabases.add(new UpdateType(updateType));
	
		// Looks like DAAP supports multibe Databases but iTunes
		// shows only one (it requests the data for the others but
		// it doesn't show the DBs, so I'm not dealting with this)
		
		serverDatabases.add(new SpecifiedTotalCount(1));
		serverDatabases.add(new ReturnedCount(1));
		
		Listing listing = new Listing();
		
		//Iterator it = databases.iterator();
		//while(it.hasNext()) {
			ListingItem listingItem = new ListingItem();
			//Database database = (Database)it.next();
			
			listingItem.add(new ItemId(current.getId()));
			listingItem.add(new PersistentId(current.getPersistentId()));
			listingItem.add(new ItemName(current.getName()));
			
			listingItem.add(new ItemCount(current.getItems(updateType).size()));
			listingItem.add(new ContainerCount(current.getContainers().size()));
			
			listing.add(listingItem);
		//}
		
		serverDatabases.add(listing);
		return serverDatabases;
	}
	
	public int size() {
		if (current==null) {
			return 0;
		} else {
			return current.getItems(false).size();
		}
	}
	
	public String toString() {
		return getName();
	}
}
