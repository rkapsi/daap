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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.kapsi.net.daap.chunks.ChunkClasses;
import de.kapsi.net.daap.chunks.ContentCode;
import de.kapsi.net.daap.chunks.impl.ContainerCount;
import de.kapsi.net.daap.chunks.impl.ContentCodesResponse;
import de.kapsi.net.daap.chunks.impl.DaapProtocolVersion;
import de.kapsi.net.daap.chunks.impl.DatabaseCount;
import de.kapsi.net.daap.chunks.impl.DmapProtocolVersion;
import de.kapsi.net.daap.chunks.impl.ItemCount;
import de.kapsi.net.daap.chunks.impl.ItemId;
import de.kapsi.net.daap.chunks.impl.ItemName;
import de.kapsi.net.daap.chunks.impl.Listing;
import de.kapsi.net.daap.chunks.impl.ListingItem;
import de.kapsi.net.daap.chunks.impl.LoginRequired;
import de.kapsi.net.daap.chunks.impl.PersistentId;
import de.kapsi.net.daap.chunks.impl.ReturnedCount;
import de.kapsi.net.daap.chunks.impl.ServerDatabases;
import de.kapsi.net.daap.chunks.impl.ServerInfoResponse;
import de.kapsi.net.daap.chunks.impl.SpecifiedTotalCount;
import de.kapsi.net.daap.chunks.impl.Status;
import de.kapsi.net.daap.chunks.impl.SupportsAutoLogout;
import de.kapsi.net.daap.chunks.impl.SupportsBrowse;
import de.kapsi.net.daap.chunks.impl.SupportsExtensions;
import de.kapsi.net.daap.chunks.impl.SupportsIndex;
import de.kapsi.net.daap.chunks.impl.SupportsPersistentIds;
import de.kapsi.net.daap.chunks.impl.SupportsQuery;
import de.kapsi.net.daap.chunks.impl.SupportsResolve;
import de.kapsi.net.daap.chunks.impl.SupportsUpdate;
import de.kapsi.net.daap.chunks.impl.TimeoutInterval;
import de.kapsi.net.daap.chunks.impl.UpdateType;

/**
 * This class and its internals are the heart of this DAAP implementation. All 
 * modifiying operations must be performed as a "transaction" on the Library. 
 * 
 * <p><code>
 * Transaction txn = library.open(false);
 * library.setName(txn, "New Name");
 * library.add(txn, new Database("Foobar"));
 * ...
 * txn.commit();
 * </code></p>
 * 
 * @author Roger Kapsi
 */
public class Library {

    private static final Log LOG = LogFactory.getLog(Library.class);

    public static final int DEFAULT_KEEP_REVISIONS = 10;
    private static final int GC_TIMER_INTERVAL = 10*1000; // 10 seconds
    
    private static long persistentId = 0;
    private int revision = 0;

    private ArrayList databases = new ArrayList();
    
    private boolean useLibraryGC;
    private LibraryRevision[] revisions;
    private Timer gcTimer = null;
    
    private final ItemName itemName = new ItemName();
    private byte[] contentCodes;

    /**
     * Creates a new Library with the provided <tt>name</tt> and with the
     * default revision history of <tt>DEFAULT_KEEP_REVISIONS</tt>
     */
    public Library(String name) {
        this(name, DEFAULT_KEEP_REVISIONS, true);
    }

    /**
     * Creates a new Library with the provided <tt>name</tt> and the max
     * number of revisions.
     */
    public Library(String name, int keepNumRevisions, boolean useLibraryGC) {

        if (keepNumRevisions <= 0)
            throw new IllegalArgumentException("keepNumRevisions must be >= 1");

        this.useLibraryGC = (useLibraryGC && keepNumRevisions > 1);
        revisions = new LibraryRevision[keepNumRevisions];
        
        itemName.setValue(name);
        contentCodes = new ContentCodesResponseImpl().getBytes();
    }

    /**
     * Returns the current revision of this library. Everytime you open() and
     * close() the library the revision will be increased by one
     */
    public synchronized int getRevision() {
        return revision;
    }
       
    /**
     * Sets the name of this Library. Note: Library must be open or an
     * <tt>IllegalStateException</tt> will be thrown
     */
    public void setName(Transaction txn, String name) {
        if (!txn.isOpen()) {
            throw new DaapException("Transaction is not open");
        }

        LibraryTxn obj = (LibraryTxn) txn.getAttribute(this);

        if (obj == null) {
            throw new DaapException();
        }

        obj.setName(name);
    }

    /**
     * Returns the name of this Library
     */
    public String getName() {
        return itemName.getValue();
    }

    /**
     * Adds database to this Library (<b>NOTE</b>: only one Database per Library
     * is supported by iTunes!)
     * 
     * @param database
     * @throws DaapTransactionException
     */
    public void add(Transaction txn, Database database) throws DaapException {
        if (!txn.isOpen()) {
            throw new DaapException("Transaction is not open");
        }

        LibraryTxn obj = (LibraryTxn) txn.getAttribute(this);

        if (obj == null) {
            throw new DaapException();
        }

        obj.add(database);
    }

    /**
     * Removes database from this Library
     * 
     * @param database
     * @throws DaapTransactionException
     */
    public void remove(Transaction txn, Database database) throws DaapException {
        if (!txn.isOpen()) {
            throw new DaapException("Transaction is not open");
        }

        LibraryTxn obj = (LibraryTxn) txn.getAttribute(this);

        if (obj == null) {
            throw new DaapException();
        }

        obj.remove(database);
    }

    /**
     * Returns true if this Library contains database
     * 
     * @param database
     * @return
     */
    public boolean contains(Database database) {
        return databases.contains(database);
    }
    
    /**
     * Same as Library.open(true)
     * @return
     */
    public synchronized Transaction open() {
        return open(true);
    }
    
    /**
     * 
     * @param autoCommit
     * @return
     */
    public synchronized Transaction open(boolean autoCommit) {
        Txn rootTxn = new LibraryTxn(this);
        Transaction txn = new Transaction(this, rootTxn, autoCommit);
        Iterator it = databases.iterator();
        while(it.hasNext()) {
            ((Database)it.next()).openTxn(txn);
        }
        return txn;
    }
    
    /**
     * Returns some kind of Object or null if <tt>request</tt> didn't matched
     * for this Library (unknown request, unknown id, whatever). The returned
     * Object could be basically anything but it's in our case either an
     * <tt>java.lang.Integer</tt> or a byte-Array (gzip'ed).
     */
    public synchronized Object select(DaapRequest request) {

        if (request.isServerInfoRequest()) {

            DaapConnection connection = request.getConnection();
            int version = connection.getProtocolVersion();

            if (version >= DaapUtil.VERSION_3) {
                LibraryRevision currentRevision = getLibraryRevision(request);
                
                if (currentRevision == null) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Unknown revision: "
                                + request.getRevisionNumber());
                    }

                    return null;
                }
                
                return currentRevision.serverInfo;
            } 
            
            return null;

        } else if (request.isContentCodesRequest()) {
            return contentCodes;

        } else if (request.isUpdateRequest()) {

            int delta = request.getDelta();

            // What's the next revision of the database
            // iTunes should ask for?
            if (delta == DaapUtil.UNDEF_VALUE) {

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
            
            LibraryRevision currentRevision = getLibraryRevision(request);

            if (currentRevision == null) {

                if (LOG.isInfoEnabled()) {
                    LOG.info("Unknown revision: "
                            + request.getRevisionNumber());
                }

                return null;
            }

            if (request.isUpdateType()) {
                return currentRevision.serverDatabasesUpdate;
            } else {
                return currentRevision.serverDatabases;
            }

        } else if (request.isSongRequest() || request.isDatabaseSongsRequest()
                || request.isDatabasePlaylistsRequest()
                || request.isPlaylistSongsRequest()) {
            
            LibraryRevision currentRevision = getLibraryRevision(request);
            
            if (currentRevision == null) {

                if (LOG.isInfoEnabled()) {
                    LOG.info("Unknown revision: "
                            + request.getRevisionNumber());
                }

                return null;
            }
            
            Database database = currentRevision.getDatabase(request);
            
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
     * Retruns the requested revision or null if the requested
     * revision is unknown
     * 
     * @param request
     * @return
     */
    private LibraryRevision getLibraryRevision(DaapRequest request) {
        int revisionNumber = request.getRevisionNumber();

        if (revisionNumber == DaapUtil.UNDEF_VALUE) {
            revisionNumber = revision;
        }
        
        LibraryRevision rev = revisions[(revisionNumber % revisions.length)];
        if (rev != null && rev.revision == revisionNumber) {
            return rev;
        } else {
            return null;
        }
    }

    /**
     * Returns the number of Databases
     * 
     * @return
     */
    public int size() {
        return databases.size();
    }
 
    static synchronized long nextPersistentId() {
        return ++persistentId;
    }

    /**
     * 
     */
    private static final class LibraryTxn implements Txn {

        private Library library;
        private String name;
        
        private HashSet databases = new HashSet();
        private HashSet deletedDatabases = new HashSet();

        private LibraryTxn(Library library) {
            this.library = library;
            this.name = library.getName();
        }

        private void setName(String name) {
            this.name = name;
        }

        private void add(Database database) {
            if (!databases.contains(database)) {
                databases.add(database);
                deletedDatabases.remove(database);
            }
        }

        private void remove(Database database) {
            if (!deletedDatabases.contains(database)) {
                deletedDatabases.add(database);
                databases.remove(database);
            }
        }

        public void commit(Transaction txn) {
            synchronized(library) {
                
                // Step 1
                // copy
                if (library.getName() != name) {
                    library.itemName.setValue(name);
                }
    
                Iterator it = databases.iterator();
                while (it.hasNext()) {
                    Database database = (Database) it.next();
                    if (!library.databases.contains(database)) {
                        library.databases.add(database);
                    }
                }
    
                it = deletedDatabases.iterator();
                while (it.hasNext()) {
                    Database database = (Database) it.next();
                    library.databases.remove(database);
                }
                
                // Step 2
                // commit
                LibraryRevision current = library.revisions[(library.revision % library.revisions.length)];
                
                if (current != null) {
                    current.databases = new ArrayList();
                }
                
                try {
                    it = library.databases.iterator();
                    while (it.hasNext()) {
                        Database database = (Database) it.next();
                        if (current != null) {
                            current.databases.add(database.clone()); // create a clone before...
                        }
                        
                        // ...commiting the new items!!!
                        Txn obj = txn.getAttribute(database);
                        if (obj != null)
                            obj.commit(txn);
                    }
                } catch (CloneNotSupportedException err) {
                    throw new DaapException(err);
                }
                
                library.revision++;
                LibraryRevision newRevision = new LibraryRevision(library.revision);
                newRevision.databases = library.databases;
                
                // 3.0.0 (iTunes 4.5 and up)
                newRevision.serverInfo = new ServerInfoResponseImpl(library, DaapUtil.VERSION_3).getBytes();
                
                newRevision.serverDatabases = new ServerDatabasesImpl(library, false).getBytes();
                newRevision.serverDatabasesUpdate = new ServerDatabasesImpl(library, true).getBytes();
                
                library.revisions[(library.revision % library.revisions.length)] = newRevision; 
                
                // Step 3
                // cleanup
                if (library.useLibraryGC && library.gcTimer == null) {
                    library.gcTimer = new Timer(true);
                    library.gcTimer.scheduleAtFixedRate(new LibraryGC(library), GC_TIMER_INTERVAL, GC_TIMER_INTERVAL);
                }
            }
            
            databases.clear();
            deletedDatabases.clear();
        }

        public void rollback(Transaction txn) {
            synchronized(library) {
                Iterator it = library.databases.iterator();
                while (it.hasNext()) {
                    Database database = (Database)it.next();
                    Txn obj = txn.getAttribute(database);
                    if (obj != null)
                        obj.rollback(txn);
                }
            }
            
            databases.clear();
            deletedDatabases.clear();
        }
        
        public void cleanup(Transaction txn) {
            synchronized(library) {
                Iterator it = databases.iterator();
                while (it.hasNext()) {
                    Database database = (Database) it.next();
                    Txn obj = txn.getAttribute(database);
                    if (obj != null)
                        obj.cleanup(txn);
                }
            }
            
            databases.clear();
            deletedDatabases.clear();
        }
        
        public void join(Txn value) {
            LibraryTxn obj = (LibraryTxn)value;
           
            if (obj.name != name)
                name = obj.name;
            
            Iterator it = obj.databases.iterator();
            while(it.hasNext()) {
                add((Database)it.next());
            }
            
            it = obj.deletedDatabases.iterator();
            while(it.hasNext()) {
                remove((Database)it.next());
            }
        }
    }
    
    /**
     * A simple Garbage Collector for the Library which deletes the
     * eldest 'LibraryRevision' object and terminates itself as soon
     * as only the latest revision is left over.
     */
    private static final class LibraryGC extends TimerTask {
        
        private Library library;
        
        private LibraryGC(Library library) {
            this.library = library;
        }
        
        public void run() {
            synchronized(library) {
                final int index = library.revision % library.revisions.length;
                int i = (index + 1) % library.revisions.length;
                while(i != index) {
                    
                    if (library.revisions[i] != null) {
                        //System.out.println("GC: " + library.revisions[i]);
                        library.revisions[i] = null;
                        break;
                    }
                    
                    i = (i + 1) % library.revisions.length;
                }
                
                if (i == index) {
                    //System.out.println("LibraryGC is done");
                    cancel();
                    library.gcTimer.cancel();
                    library.gcTimer = null;
                }
            }
        }
    }
    
    /**
     * 
     */
    private static final class LibraryRevision {
        
        private final int revision;
        private ArrayList databases;
        
        private byte[] serverInfo;
        private byte[] serverDatabases;
        private byte[] serverDatabasesUpdate;
        
        private LibraryRevision(int revision) {
            this.revision = revision;
        }
        
        /**
         * Returns a Database for the <tt>request</tt>. The requested Database is
         * determinated by the Database ID.
         */
        private Database getDatabase(DaapRequest request) {

            int revisionNumber = request.getRevisionNumber();

            if (revisionNumber == DaapUtil.UNDEF_VALUE) {
                revisionNumber = revision;
            }

            if (databases != null && !databases.isEmpty()) {
                int databaseId = request.getDatabaseId();
                if (databaseId == DaapUtil.UNDEF_VALUE) {
                    return (Database) databases.get(0);
                } else {
                    Iterator it = databases.iterator();
                    while (it.hasNext()) {
                        Database database = (Database) it.next();
                        if (database.getId() == databaseId) {
                            return database;
                        }
                    }
                }
            }

            return null;
        }
        
        public String toString() {
            return "LibraryRevision: " + revision;
        }
    }
    
    /**
     * This class implements the ServerDatabases chunk.
     */
    private static final class ServerDatabasesImpl extends ServerDatabases {

        public ServerDatabasesImpl(Library library, boolean updateType) {
            super();

            add(new Status(200));
            add(new UpdateType(updateType));

            add(new SpecifiedTotalCount(library.databases.size()));
            add(new ReturnedCount(library.databases.size()));

            Listing listing = new Listing();

            Iterator it = library.databases.iterator();
            while (it.hasNext()) {
                ListingItem listingItem = new ListingItem();

                Database database = (Database) it.next();

                listingItem.add(new ItemId(database.getId()));
                listingItem.add(new PersistentId(database.getPersistentId()));
                listingItem.add(new ItemName(database.getName()));

                Playlist playlist = database.getMasterPlaylist();
                int itemCount = ((updateType) ? playlist.getNewSongs()
                        : playlist.getSongs()).size();
                int containerCount = database.getPlaylists().size();

                listingItem.add(new ItemCount(itemCount));
                listingItem.add(new ContainerCount(containerCount));

                listing.add(listingItem);
            }

            add(listing);
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
     * Groups many ContentCodes to one single chunk
     */
    private static final class ContentCodesResponseImpl extends
            ContentCodesResponse {

        public ContentCodesResponseImpl() {
            super();

            add(new Status(200));

            String[] names = ChunkClasses.names;

            final Class[] arg1 = new Class[] {};
            final Object[] arg2 = new Object[] {};

            for (int i = 0; i < names.length; i++) {
                try {
                    Class clazz = Class.forName(names[i]);

                    Method methodContentCode = clazz.getMethod(
                            "getContentCode", arg1);
                    Method methodName = clazz.getMethod("getName", arg1);
                    Method methodType = clazz.getMethod("getType", arg1);

                    Object inst = clazz.newInstance();

                    String contentCode = (String) methodContentCode.invoke(
                            inst, arg2);
                    String name = (String) methodName.invoke(inst, arg2);
                    int type = ((Integer) methodType.invoke(inst, arg2))
                            .intValue();

                    add(new ContentCode(contentCode, name, type));

                } catch (ClassNotFoundException err) {
                    LOG.error(err);
                } catch (NoSuchMethodException err) {
                    LOG.error(err);
                } catch (InstantiationException err) {
                    LOG.error(err);
                } catch (IllegalAccessException err) {
                    LOG.error(err);
                } catch (IllegalArgumentException err) {
                    LOG.error(err);
                } catch (InvocationTargetException err) {
                    LOG.error(err);
                } catch (SecurityException err) {
                    LOG.error(err);
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
     * This class implements the ServerInfoResponse
     */
    private static final class ServerInfoResponseImpl extends
            ServerInfoResponse {

        public ServerInfoResponseImpl(Library library, int version) {
            super();

            add(new Status(200));
            add(new TimeoutInterval(1800));
            add(new DmapProtocolVersion(version));
            add(new DaapProtocolVersion(version));
            add(library.itemName);
            add(new LoginRequired(false));
            add(new SupportsAutoLogout(false));
            add(new SupportsUpdate(false));
            add(new SupportsPersistentIds(false));
            add(new SupportsExtensions(false));
            add(new SupportsBrowse(false));
            add(new SupportsQuery(false));
            add(new SupportsIndex(false));
            add(new SupportsResolve(false));
            add(new DatabaseCount(library.databases.size()));
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
}