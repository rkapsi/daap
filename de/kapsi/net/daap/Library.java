
package de.kapsi.net.daap;

import java.util.Iterator;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.chunks.ServerDatabases;

public class Library {
	
    private static final Log LOG = LogFactory.getLog(Library.class);
	
	private static final int DATABASE_ID = 1;

	private ArrayList revisions = new ArrayList();
	
	private int keepNumRevisions;
	private String name;
	
	private Database current;
	private Database temp;
	
    private ServerDatabases serverDatabases;
    private ServerDatabases serverDatabasesUpdate;
    
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
	
	public boolean isOpen() {
		return open;
	}
	
    public void delete() {
        if (open) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Library is open.");
			}
			return;
		}
        
        revisions.clear();

        current = null;
        temp = null;
	
        serverDatabases = null;
        serverDatabasesUpdate = null;
    }

	public void open() {
		
		if (open) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Library is already opened for edit");
			}
			return;
		}
		
		if (current == null) {
			// current is initialized on close()! 
			temp = new Database(DATABASE_ID, name, "0");
			
		} else {
			
			temp = current;
			current = temp.createSnapshot();
			
			temp.open();
		}
		
		open = true;
	}
	
	public void close() {
		if (!open) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Library wasn't opened for edit");
			}
			return;
		}
		
		if (current != null) {
			revisions.add(current);
		}

		current = temp;
        current.close();
		temp = null;
		
        ArrayList databases = new ArrayList();
        databases.add(current);
        
        serverDatabases = new ServerDatabasesImpl(databases, false);
        serverDatabasesUpdate = new ServerDatabasesImpl(databases, true);
        
		if (revisions.size() >= keepNumRevisions) {
			Database old = (Database)revisions.remove(0);
			old.destroy();
		}
        
        open = false;
	}
	
	public synchronized Object select(DaapRequest request) {
	
		if (request.isUpdateRequest()) {
			
            int delta = request.getDelta();
        
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
					LOG.info("No database with this revision known: " + request.getRevisionNumber());
				}
				
				return null;
			}
            
            if (request.isUpdateType()) {
                return serverDatabasesUpdate;
            } else {
                return serverDatabases;
            }
			
		} else if (request.isSongRequest() 
					|| request.isDatabaseSongsRequest() 
					|| request.isDatabasePlaylistsRequest()
					|| request.isPlaylistSongsRequest()) {
			
			Database database = getDatabase(request);
			if (database == null) {
				if (LOG.isInfoEnabled()) {
					LOG.info("No database with this revision known: " + request.getRevisionNumber());
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
	
	public void add(Song song) {
    
		if (!open) {
			throw new IllegalStateException();
		}
		
		temp.getMasterPlaylist().add(song);
	}
	
	public boolean remove(Song song) {
	
		if (!open) {
			throw new IllegalStateException();
		}
		
		return temp.getMasterPlaylist().remove(song);
	}
	
	public void add(Playlist playlist) {
		
		if (!open) {
			throw new IllegalStateException();
		}
		
		temp.add(playlist);
	}
	
	public boolean remove(Playlist playlist) {
	
		if (!open) {
			throw new IllegalStateException();
		}
		
		return temp.remove(playlist);
	}
	
	public int size() {
		if (current==null) {
			return 0;
		} else {
    
            Playlist masterPlaylist = current.getMasterPlaylist();
            if (masterPlaylist == null && temp != null)
                masterPlaylist = temp.getMasterPlaylist();
            
            if (masterPlaylist != null) {
                return masterPlaylist.size();
            } else {
                return 0;
            }
		}
	}
	
	public String toString() {
		return getName();
	}
}
