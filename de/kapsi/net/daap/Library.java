
package de.kapsi.net.daap;

import java.util.Iterator;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.chunks.ServerDatabases;

/**
 * This class and its internals are the heart of this DAAP
 * implementation. Note: it's very important to synchronize
 * all operations!!!<p>
 * <code>
 * Library library = ...;
 * synchronized(library) {
 *      library.open();
 *      library.add(new Song(...));
 *      library.close();
 *  }
 * </code>
 */
public class Library {
	
    private static final Log LOG = LogFactory.getLog(Library.class);
	
    private static final int DATABASE_ID = 1;
    
    public static final int DEFAULT_KEEP_REVISIONS = 10;
    
    private ArrayList revisions = new ArrayList();

    private int keepNumRevisions;
    private String name;

    private Database current;
    private Database temp;
	
    private ServerDatabases serverDatabases;
    private ServerDatabases serverDatabasesUpdate;
    
    private boolean open = false;

    /**
     * Creates a new Library with the provided <tt>name</tt>
     * and with the default revision history of <tt>DEFAULT_KEEP_REVISIONS</tt>
     */
    public Library(String name) {
        this(name, DEFAULT_KEEP_REVISIONS);
    }

    /**
     * Creates a new Library with the provided <tt>name</tt> and
     * the max number of revisions.
     */
    public Library(String name, int keepNumRevisions) {
        
        if (keepNumRevisions <= 0)
            throw new IllegalArgumentException("keepNumRevisions must be >= 1");
        
        this.name = name;
        this.keepNumRevisions = keepNumRevisions;
    }
    
    /**
     * Returns the current revision of this library. Everytime
     * you open() and close() the library the revision will be
     * increased by one
     */
    private int getRevision() {
        if (current == null) {
            return 0;
        } else {
            return current.getRevision();
        }
    }

    /**
     * Sets the name of this Library. Note: Library must be
     * open or an <tt>IllegalStateException</tt> will be thrown
     */
    public void setName(String name) {
        if (!isOpen()) {
            throw new IllegalStateException("Library is not open");
        }

        temp.setName(name);
    }

    /**
     * Returns the name of this Library
     */
    public String getName() {
        if (current == null) {
            return name;
        } else {
            return current.getName();
        }
    }

    /**
     * Returns <tt>true</tt> if Library is open
     * an can be edited
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Deletes everything from the Library. Note: you
     * should shutdown the server before doing this!
     */
    public void delete() {
        if (isOpen()) {
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

    /**
     * Creates an empty Library
     */
    public void init() {
        if (getRevision()==0) {
            open();
            close();
        }
    }
    
    /**
     * Open the Library for edit
     */
    public void open() {

        if (isOpen()) {
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

    /**
     * Closes the Library
     */
    public  void close() {
        if (!isOpen()) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Library is already closed");
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

    /**
     * Returns some kind of Object or null if <tt>request</tt>
     * didn't matched for this Library (unknown request, unknown id,
     * whatever). The returned Object could be basically anything
     * but it's in our case either an <tt>java.lang.Integer</tt> or
     * a child of Chunk.
     */
    public synchronized Object select(DaapRequest request) {

        if (request.isUpdateRequest()) {

            int delta = request.getDelta();

            // What's the next revision of the database 
            // iTunes should ask for?
            if (delta == DaapRequest.UNDEF_VALUE) { 
                 
                // 1st. request, iTunes should/will 
                // ask for the current revision
                return (new Integer(getRevision()));

            } else if (delta < getRevision()) {
                
                // ask for the next revision
                return (new Integer(++delta)); 

            } else {

                // iTunes is up-to-date
                return (new Integer(delta));
            }

        } else if (request.isDatabasesRequest()) {

            Database database = getDatabase(request);

            if (database == null) {

                if (LOG.isInfoEnabled()) {
                    LOG.info("No database with this revision known: " 
                        + request.getRevisionNumber());
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
                    LOG.info("No database with this revision known: " 
                        + request.getRevisionNumber());
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

    /**
     * Returns a Database for the <tt>request</tt>. The
     * requested Database is determinated by the Database ID.
     */
    private Database getDatabase(DaapRequest request) {

        if (current == null) {
            return null;
        }

        int revisionNumber = request.getRevisionNumber();
        
        if (revisionNumber == DaapRequest.UNDEF_VALUE || 
                       revisionNumber == current.getRevision()) {
                           
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

    /**
     * Adds <tt>song</tt> to the Master Playlist.
     */
    public void add(Song song) {

        if (!isOpen()) {
            throw new IllegalStateException("Library is not open");
        }

        temp.add(song);
    }

    /**
     * Removes <tt>song</tt> from the Master Playlist.
     */
    public boolean remove(Song song) {

        if (!isOpen()) {
            throw new IllegalStateException("Library is not open");
        }

        return temp.remove(song);
    }

    /**
     * Adds <tt>playlist</tt> to the Library
     */
    public void add(Playlist playlist) {

        if (!isOpen()) {
            throw new IllegalStateException("Library is not open");
        }

        temp.add(playlist);
    }

    /**
     * Removes <tt>playlist</tt> from the Library
     */
    public boolean remove(Playlist playlist) {

        if (!isOpen()) {
            throw new IllegalStateException("Library is not open");
        }

        return temp.remove(playlist);
    }

    /**
     * Returns the number of Songs in this Library
     */
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
